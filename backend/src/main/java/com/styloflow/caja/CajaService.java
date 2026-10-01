package com.styloflow.caja;

import com.styloflow.common.BusinessException;
import com.styloflow.common.NotFoundException;
import com.styloflow.common.PageResponse;
import com.styloflow.usuarios.UsuarioRepository;
import com.styloflow.ventas.MetodoPago;
import com.styloflow.ventas.VentaRepository;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CajaService {

    public record AbrirRequest(
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal montoInicial,
            @Size(max = 500) String observaciones) {}

    public record CerrarRequest(
            @NotNull @DecimalMin("0.00") @Digits(integer = 10, fraction = 2) BigDecimal efectivoContado,
            @Size(max = 500) String observaciones) {}

    public record TotalMetodo(MetodoPago metodo, long cantidad, BigDecimal total) {}

    public record CajaResponse(Long id, Caja.Estado estado, String abiertaPor, Instant abiertaEn,
            BigDecimal montoInicial, String cerradaPor, Instant cerradaEn, long cantidadVentas,
            BigDecimal totalVentas, BigDecimal efectivoEsperado, BigDecimal efectivoContado, BigDecimal diferencia,
            List<TotalMetodo> porMetodo, String observaciones) {}

    private final CajaRepository cajas;
    private final VentaRepository ventas;
    private final UsuarioRepository usuarios;
    private final Clock clock;

    @Transactional(readOnly = true)
    public Optional<CajaResponse> actual() {
        return cajas.findFirstByEstado(Caja.Estado.ABIERTA).map(this::toResponse);
    }

    /** Caja abierta, requerida para registrar ventas. */
    public Caja abiertaOFallar() {
        return cajas.findFirstByEstado(Caja.Estado.ABIERTA)
                .orElseThrow(() -> new BusinessException("No hay una caja abierta. Abra caja antes de vender."));
    }

    @Transactional
    public CajaResponse abrir(AbrirRequest req, Long usuarioId) {
        if (cajas.findFirstByEstado(Caja.Estado.ABIERTA).isPresent()) {
            throw new BusinessException("Ya existe una caja abierta");
        }
        Caja c = new Caja();
        c.setEstado(Caja.Estado.ABIERTA);
        c.setAbiertaPor(usuarios.getReferenceById(usuarioId));
        c.setAbiertaEn(clock.instant());
        c.setMontoInicial(req.montoInicial());
        c.setObservaciones(req.observaciones());
        return toResponse(cajas.saveAndFlush(c));
    }

    @Transactional
    public CajaResponse cerrar(CerrarRequest req, Long usuarioId) {
        Caja c = abiertaOFallar();
        List<TotalMetodo> porMetodo = totales(c.getId());
        BigDecimal esperado = c.getMontoInicial().add(totalDe(porMetodo, MetodoPago.EFECTIVO));
        c.setEstado(Caja.Estado.CERRADA);
        c.setCerradaPor(usuarios.getReferenceById(usuarioId));
        c.setCerradaEn(clock.instant());
        c.setTotalVentas(porMetodo.stream().map(TotalMetodo::total).reduce(BigDecimal.ZERO, BigDecimal::add));
        c.setEfectivoEsperado(esperado);
        c.setEfectivoContado(req.efectivoContado());
        c.setDiferencia(req.efectivoContado().subtract(esperado));
        if (req.observaciones() != null && !req.observaciones().isBlank()) {
            c.setObservaciones(req.observaciones());
        }
        cajas.flush();
        return toResponse(c);
    }

    @Transactional(readOnly = true)
    public PageResponse<CajaResponse> historial(int page, int size) {
        return PageResponse.of(cajas.findAllByOrderByAbiertaEnDesc(PageRequest.of(page, Math.min(size, 100))),
                this::toResponse);
    }

    @Transactional(readOnly = true)
    public CajaResponse detalle(Long id) {
        return cajas.findWithUsuariosById(id).map(this::toResponse).orElseThrow(() -> new NotFoundException("Caja", id));
    }

    private CajaResponse toResponse(Caja c) {
        List<TotalMetodo> porMetodo = totales(c.getId());
        long cantidad = porMetodo.stream().mapToLong(TotalMetodo::cantidad).sum();
        BigDecimal total = porMetodo.stream().map(TotalMetodo::total).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal esperado = c.isAbierta()
                ? c.getMontoInicial().add(totalDe(porMetodo, MetodoPago.EFECTIVO))
                : c.getEfectivoEsperado();
        return new CajaResponse(c.getId(), c.getEstado(), c.getAbiertaPor().getNombre(), c.getAbiertaEn(),
                c.getMontoInicial(), c.getCerradaPor() != null ? c.getCerradaPor().getNombre() : null,
                c.getCerradaEn(), cantidad, total, esperado, c.getEfectivoContado(), c.getDiferencia(), porMetodo,
                c.getObservaciones());
    }

    private List<TotalMetodo> totales(Long cajaId) {
        return ventas.totalesPorMetodo(cajaId).stream()
                .map(t -> new TotalMetodo(t.getMetodoPago(), t.getCantidad(), t.getTotal()))
                .toList();
    }

    private static BigDecimal totalDe(List<TotalMetodo> totales, MetodoPago metodo) {
        return totales.stream().filter(t -> t.metodo() == metodo).map(TotalMetodo::total)
                .findFirst().orElse(BigDecimal.ZERO);
    }
}
