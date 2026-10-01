package com.styloflow.ventas;

import com.styloflow.auth.CurrentUser;
import com.styloflow.common.PageResponse;
import com.styloflow.ventas.VentaDtos.AnularRequest;
import com.styloflow.ventas.VentaDtos.VentaRequest;
import com.styloflow.ventas.VentaDtos.VentaResponse;
import com.styloflow.ventas.VentaDtos.VentaResumen;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
public class VentaController {

    private final VentaService service;
    private final CurrentUser currentUser;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public VentaResponse crear(@Valid @RequestBody VentaRequest req) {
        return service.crear(req, currentUser.id());
    }

    @GetMapping
    public PageResponse<VentaResumen> buscar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate desde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate hasta,
            @RequestParam(required = false) Long clienteId,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.buscar(desde, hasta, clienteId, page, size);
    }

    @GetMapping("/{id}")
    public VentaResponse obtener(@PathVariable Long id) {
        return service.obtener(id);
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMIN')")
    public VentaResponse anular(@PathVariable Long id, @Valid @RequestBody AnularRequest req) {
        return service.anular(id, req.motivo(), currentUser.id());
    }
}
