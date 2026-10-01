package com.styloflow.catalogo;

import com.styloflow.catalogo.CatalogoDtos.CategoriaRequest;
import com.styloflow.catalogo.CatalogoDtos.CategoriaResponse;
import com.styloflow.catalogo.CatalogoDtos.ProductoRequest;
import com.styloflow.catalogo.CatalogoDtos.ProductoResponse;
import com.styloflow.catalogo.CatalogoDtos.ServicioRequest;
import com.styloflow.catalogo.CatalogoDtos.ServicioResponse;
import com.styloflow.common.BusinessException;
import com.styloflow.common.NotFoundException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CatalogoService {

    private final CategoriaRepository categorias;
    private final ServicioRepository servicios;
    private final ProductoRepository productos;

    // ---- Categorías ----

    @Transactional(readOnly = true)
    public List<CategoriaResponse> listarCategorias() {
        return categorias.findAllByOrderByNombreAsc().stream().map(CategoriaResponse::from).toList();
    }

    @Transactional
    public CategoriaResponse crearCategoria(CategoriaRequest req) {
        if (categorias.existsByNombreIgnoreCase(req.nombre().trim())) {
            throw new BusinessException("Ya existe una categoría con ese nombre");
        }
        Categoria c = new Categoria();
        c.setNombre(req.nombre().trim());
        if (req.activo() != null) {
            c.setActivo(req.activo());
        }
        return CategoriaResponse.from(categorias.save(c));
    }

    @Transactional
    public CategoriaResponse actualizarCategoria(Long id, CategoriaRequest req) {
        Categoria c = categoria(id);
        c.setNombre(req.nombre().trim());
        if (req.activo() != null) {
            c.setActivo(req.activo());
        }
        return CategoriaResponse.from(c);
    }

    @Transactional
    public void eliminarCategoria(Long id) {
        if (servicios.countByCategoriaId(id) > 0) {
            throw new BusinessException("La categoría tiene servicios; desactívela en lugar de eliminarla");
        }
        categorias.delete(categoria(id));
    }

    // ---- Servicios ----

    @Transactional(readOnly = true)
    public List<ServicioResponse> listarServicios(boolean soloActivos) {
        return servicios.findAllConCategoria().stream()
                .filter(s -> !soloActivos || (s.isActivo() && s.getCategoria().isActivo()))
                .map(ServicioResponse::from)
                .toList();
    }

    @Transactional
    public ServicioResponse crearServicio(ServicioRequest req) {
        Servicio s = new Servicio();
        aplicar(s, req);
        return ServicioResponse.from(servicios.save(s));
    }

    @Transactional
    public ServicioResponse actualizarServicio(Long id, ServicioRequest req) {
        Servicio s = servicios.findById(id).orElseThrow(() -> new NotFoundException("Servicio", id));
        aplicar(s, req);
        return ServicioResponse.from(s);
    }

    private void aplicar(Servicio s, ServicioRequest req) {
        s.setCategoria(categoria(req.categoriaId()));
        s.setNombre(req.nombre().trim());
        s.setDescripcion(req.descripcion());
        s.setDuracionMin(req.duracionMin());
        s.setPrecio(req.precio());
        if (req.activo() != null) {
            s.setActivo(req.activo());
        }
    }

    // ---- Productos ----

    @Transactional(readOnly = true)
    public List<ProductoResponse> listarProductos(boolean soloActivos) {
        return productos.findAllByOrderByNombreAsc().stream()
                .filter(p -> !soloActivos || p.isActivo())
                .map(ProductoResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ProductoResponse> stockBajo() {
        return productos.findStockBajo().stream().map(ProductoResponse::from).toList();
    }

    @Transactional
    public ProductoResponse crearProducto(ProductoRequest req) {
        if (req.sku() != null && !req.sku().isBlank() && productos.existsBySkuIgnoreCase(req.sku().trim())) {
            throw new BusinessException("Ya existe un producto con ese SKU");
        }
        Producto p = new Producto();
        aplicar(p, req);
        return ProductoResponse.from(productos.save(p));
    }

    @Transactional
    public ProductoResponse actualizarProducto(Long id, ProductoRequest req) {
        Producto p = producto(id);
        aplicar(p, req);
        return ProductoResponse.from(p);
    }

    /** Suma (o resta, si es negativo) unidades al stock: compras, mermas, inventario. */
    @Transactional
    public ProductoResponse ajustarStock(Long id, int cantidad) {
        Producto p = productos.findByIdForUpdate(id).orElseThrow(() -> new NotFoundException("Producto", id));
        if (cantidad < 0) {
            p.descontarStock(-cantidad);
        } else {
            p.reponerStock(cantidad);
        }
        return ProductoResponse.from(p);
    }

    private static void aplicar(Producto p, ProductoRequest req) {
        p.setNombre(req.nombre().trim());
        p.setSku(req.sku() == null || req.sku().isBlank() ? null : req.sku().trim().toUpperCase());
        p.setPrecio(req.precio());
        p.setStock(req.stock());
        p.setStockMinimo(req.stockMinimo() != null ? req.stockMinimo() : 0);
        if (req.activo() != null) {
            p.setActivo(req.activo());
        }
    }

    private Categoria categoria(Long id) {
        return categorias.findById(id).orElseThrow(() -> new NotFoundException("Categoría", id));
    }

    private Producto producto(Long id) {
        return productos.findById(id).orElseThrow(() -> new NotFoundException("Producto", id));
    }
}
