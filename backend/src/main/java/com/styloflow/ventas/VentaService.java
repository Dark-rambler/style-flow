package com.styloflow.ventas;

import com.styloflow.caja.Caja;
import com.styloflow.caja.CajaService;
import com.styloflow.catalogo.Producto;
import com.styloflow.catalogo.ProductoRepository;
import com.styloflow.catalogo.Servicio;
import com.styloflow.catalogo.ServicioRepository;
import com.styloflow.clientes.ClienteService;
import com.styloflow.common.BusinessException;
import com.styloflow.common.NotFoundException;
import com.styloflow.common.PageResponse;
import com.styloflow.negocio.NegocioService;
import com.styloflow.usuarios.Usuario;
import com.styloflow.usuarios.UsuarioRepository;
import com.styloflow.ventas.VentaDtos.ItemRequest;
import com.styloflow.ventas.VentaDtos.VentaRequest;
import com.styloflow.ventas.VentaDtos.VentaResponse;
import com.styloflow.ventas.VentaDtos.VentaResumen;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class VentaService {

    private static final BigDecimal CIEN = BigDecimal.valueOf(100);

    private final VentaRepository ventas;
    private final CajaService cajaService;
    private final ServicioRepository servicios;
    private final ProductoRepository productos;
    private final UsuarioRepository usuarios;
    private final ClienteService clienteService;
    private final NegocioService negocioService;
    private final Clock clock;
    private final ZoneId zona;

    @Transactional
    public VentaResponse crear(VentaRequest req, Long cajeroId) {
        Caja caja = cajaService.abiertaOFallar();

        Venta v = new Venta();
        v.setFecha(clock.instant());
        v.setCaja(caja);
        v.setCajero(usuarios.getReferenceById(cajeroId));
        if (req.clienteId() != null) {
            v.setCliente(clienteService.buscarEntidad(req.clienteId()));
        }

        for (ItemRequest ir : req.items()) {
            v.agregarItem(crearItem(ir));
        }

        BigDecimal subtotal = v.getItems().stream().map(VentaItem::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        // subtotal ya es neto de los descuentos por línea; este es el descuento global de la venta
        BigDecimal descuento = dinero(req.descuento() != null ? req.descuento() : BigDecimal.ZERO);
        if (descuento.compareTo(subtotal) > 0) {
            throw new BusinessException("El descuento no puede superar el subtotal");
        }
        BigDecimal total = subtotal.subtract(descuento);

        v.setSubtotal(subtotal);
        v.setDescuento(descuento);
        v.setTotal(total);
        v.setIva(ivaIncluido(total, negocioService.actual().getIvaPorcentaje()));
        v.setMetodoPago(req.metodoPago());
        v.setObservaciones(req.observaciones() != null && !req.observaciones().isBlank()
                ? req.observaciones().trim() : null);

        if (req.metodoPago() == MetodoPago.EFECTIVO) {
            BigDecimal recibido = req.montoRecibido() != null ? dinero(req.montoRecibido()) : total;
            if (recibido.compareTo(total) < 0) {
                throw new BusinessException("El monto recibido es menor al total");
            }
            v.setMontoRecibido(recibido);
            v.setCambio(recibido.subtract(total));
        } else {
            v.setMontoRecibido(total);
            v.setCambio(BigDecimal.ZERO.setScale(2));
        }

        return VentaResponse.from(ventas.save(v));
    }

    private VentaItem crearItem(ItemRequest ir) {
        VentaItem item = new VentaItem();
        item.setTipo(ir.tipo());
        item.setCantidad(ir.cantidad());
        BigDecimal precioCatalogo;
        if (ir.tipo() == VentaItem.Tipo.SERVICIO) {
            Servicio s = servicios.findById(ir.itemId()).orElseThrow(() -> new NotFoundException("Servicio", ir.itemId()));
            if (!s.isActivo()) {
                throw new BusinessException("El servicio '" + s.getNombre() + "' no está activo");
            }
            item.setServicio(s);
            item.setDescripcion(s.getNombre());
            precioCatalogo = s.getPrecio();
        } else {
            Producto p = productos.findByIdForUpdate(ir.itemId())
                    .orElseThrow(() -> new NotFoundException("Producto", ir.itemId()));
            if (!p.isActivo()) {
                throw new BusinessException("El producto '" + p.getNombre() + "' no está activo");
            }
            p.descontarStock(ir.cantidad());
            item.setProducto(p);
            item.setDescripcion(p.getNombre());
            precioCatalogo = p.getPrecio();
        }
        if (ir.estilistaId() != null) {
            Usuario e = usuarios.findById(ir.estilistaId())
                    .filter(Usuario::isActivo)
                    .orElseThrow(() -> new BusinessException("Estilista inválido o inactivo: " + ir.estilistaId()));
            item.setEstilista(e);
        }
        BigDecimal precio = dinero(ir.precioUnitario() != null ? ir.precioUnitario() : precioCatalogo);
        BigDecimal bruto = precio.multiply(BigDecimal.valueOf(ir.cantidad()));
        BigDecimal descuento = dinero(ir.descuento() != null ? ir.descuento() : BigDecimal.ZERO);
        if (descuento.compareTo(bruto) > 0) {
            throw new BusinessException("El descuento de '" + item.getDescripcion() + "' supera su importe");
        }
        item.setPrecioUnitario(precio);
        item.setDescuento(descuento);
        item.setSubtotal(bruto.subtract(descuento));
        return item;
    }

    @Transactional
    public VentaResponse anular(Long id, String motivo, Long usuarioId) {
        Venta v = ventas.findDetalle(id).orElseThrow(() -> new NotFoundException("Venta", id));
        if (v.getEstado() == Venta.Estado.ANULADA) {
            throw new BusinessException("La venta ya está anulada");
        }
        if (!v.getCaja().isAbierta()) {
            throw new BusinessException("Solo se pueden anular ventas de la caja abierta");
        }
        for (VentaItem item : v.getItems()) {
            if (item.getTipo() == VentaItem.Tipo.PRODUCTO) {
                productos.findByIdForUpdate(item.getProducto().getId()).ifPresent(p -> p.reponerStock(item.getCantidad()));
            }
        }
        v.setEstado(Venta.Estado.ANULADA);
        v.setAnuladaPor(usuarios.getReferenceById(usuarioId));
        v.setAnuladaEn(clock.instant());
        v.setMotivoAnulacion(motivo.trim());
        ventas.flush();
        return VentaResponse.from(ventas.findDetalle(id).orElseThrow());
    }

    @Transactional(readOnly = true)
    public VentaResponse obtener(Long id) {
        return ventas.findDetalle(id).map(VentaResponse::from).orElseThrow(() -> new NotFoundException("Venta", id));
    }

    @Transactional(readOnly = true)
    public PageResponse<VentaResumen> buscar(LocalDate desde, LocalDate hasta, Long clienteId, int page, int size) {
        LocalDate hoy = LocalDate.now(clock.withZone(zona));
        LocalDate d = desde != null ? desde : hoy;
        LocalDate h = hasta != null ? hasta : d;
        if (h.isBefore(d)) {
            throw new BusinessException("La fecha 'hasta' no puede ser anterior a 'desde'");
        }
        return PageResponse.of(ventas.buscar(d.atStartOfDay(zona).toInstant(), h.plusDays(1).atStartOfDay(zona).toInstant(),
                clienteId, PageRequest.of(page, Math.min(size, 100))), VentaResumen::from);
    }

    /** IVA contenido en un precio que ya lo incluye: total * tasa / (100 + tasa). */
    static BigDecimal ivaIncluido(BigDecimal total, BigDecimal tasa) {
        if (tasa.signum() == 0) {
            return BigDecimal.ZERO.setScale(2);
        }
        return total.multiply(tasa).divide(CIEN.add(tasa), 2, RoundingMode.HALF_UP);
    }

    private static BigDecimal dinero(BigDecimal valor) {
        return valor.setScale(2, RoundingMode.HALF_UP);
    }
}
