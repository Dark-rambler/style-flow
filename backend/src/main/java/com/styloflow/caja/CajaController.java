package com.styloflow.caja;

import com.styloflow.auth.CurrentUser;
import com.styloflow.caja.CajaService.AbrirRequest;
import com.styloflow.caja.CajaService.CajaResponse;
import com.styloflow.caja.CajaService.CerrarRequest;
import com.styloflow.common.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'CAJERO')")
public class CajaController {

    private final CajaService service;
    private final CurrentUser currentUser;

    /** Caja abierta con totales en vivo; 204 si no hay ninguna abierta. */
    @GetMapping("/actual")
    public ResponseEntity<CajaResponse> actual() {
        return service.actual().map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/abrir")
    public CajaResponse abrir(@Valid @RequestBody AbrirRequest req) {
        return service.abrir(req, currentUser.id());
    }

    @PostMapping("/cerrar")
    public CajaResponse cerrar(@Valid @RequestBody CerrarRequest req) {
        return service.cerrar(req, currentUser.id());
    }

    @GetMapping("/historial")
    public PageResponse<CajaResponse> historial(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.historial(page, size);
    }

    @GetMapping("/{id}")
    public CajaResponse detalle(@PathVariable Long id) {
        return service.detalle(id);
    }
}
