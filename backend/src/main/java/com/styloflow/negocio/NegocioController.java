package com.styloflow.negocio;

import com.styloflow.negocio.NegocioService.NegocioDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/negocio")
@RequiredArgsConstructor
public class NegocioController {

    private final NegocioService service;

    @GetMapping
    public NegocioDto obtener() {
        return service.obtener();
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public NegocioDto actualizar(@Valid @RequestBody NegocioDto dto) {
        return service.actualizar(dto);
    }
}
