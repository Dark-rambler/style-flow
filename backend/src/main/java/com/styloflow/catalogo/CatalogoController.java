package com.styloflow.catalogo;

import com.styloflow.catalogo.CatalogoDtos.AjusteStockRequest;
import com.styloflow.catalogo.CatalogoDtos.CategoriaRequest;
import com.styloflow.catalogo.CatalogoDtos.CategoriaResponse;
import com.styloflow.catalogo.CatalogoDtos.ProductoRequest;
import com.styloflow.catalogo.CatalogoDtos.ProductoResponse;
import com.styloflow.catalogo.CatalogoDtos.ServicioRequest;
import com.styloflow.catalogo.CatalogoDtos.ServicioResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Lectura: cualquier usuario autenticado. Escritura: solo ADMIN. */
@RestController
@RequestMapping("/api/catalogo")
@RequiredArgsConstructor
public class CatalogoController {

    private final CatalogoService service;

    @GetMapping("/categorias")
    public List<CategoriaResponse> categorias() {
        return service.listarCategorias();
    }

    @PostMapping("/categorias")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaResponse crearCategoria(@Valid @RequestBody CategoriaRequest req) {
        return service.crearCategoria(req);
    }

    @PutMapping("/categorias/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public CategoriaResponse actualizarCategoria(@PathVariable Long id, @Valid @RequestBody CategoriaRequest req) {
        return service.actualizarCategoria(id, req);
    }

    @DeleteMapping("/categorias/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasRole('ADMIN')")
    public void eliminarCategoria(@PathVariable Long id) {
        service.eliminarCategoria(id);
    }

    @GetMapping("/servicios")
    public List<ServicioResponse> servicios(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return service.listarServicios(soloActivos);
    }

    @PostMapping("/servicios")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponse crearServicio(@Valid @RequestBody ServicioRequest req) {
        return service.crearServicio(req);
    }

    @PutMapping("/servicios/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ServicioResponse actualizarServicio(@PathVariable Long id, @Valid @RequestBody ServicioRequest req) {
        return service.actualizarServicio(id, req);
    }

    @GetMapping("/productos")
    public List<ProductoResponse> productos(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return service.listarProductos(soloActivos);
    }

    @GetMapping("/productos/stock-bajo")
    public List<ProductoResponse> stockBajo() {
        return service.stockBajo();
    }

    @PostMapping("/productos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse crearProducto(@Valid @RequestBody ProductoRequest req) {
        return service.crearProducto(req);
    }

    @PutMapping("/productos/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse actualizarProducto(@PathVariable Long id, @Valid @RequestBody ProductoRequest req) {
        return service.actualizarProducto(id, req);
    }

    @PostMapping("/productos/{id}/ajuste-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoResponse ajustarStock(@PathVariable Long id, @Valid @RequestBody AjusteStockRequest req) {
        return service.ajustarStock(id, req.cantidad());
    }
}
