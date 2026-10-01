package com.styloflow.negocio;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import com.styloflow.common.NotFoundException;
import com.styloflow.tenant.TenantContext;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NegocioService {

    /** {@code codigo} es solo de lectura: identifica al negocio en el login y no se modifica. */
    public record NegocioDto(
            String codigo,
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 30) String nit,
            @Size(max = 200) String direccion,
            @Size(max = 30) String telefono,
            @NotBlank @Size(min = 3, max = 3) String moneda,
            @NotBlank @Size(max = 5) String simbolo,
            @NotNull @DecimalMin("0") @DecimalMax("100") BigDecimal ivaPorcentaje,
            @Size(max = 250) String mensajeTicket) {

        static NegocioDto from(Negocio n) {
            return new NegocioDto(n.getCodigo(), n.getNombre(), n.getNit(), n.getDireccion(), n.getTelefono(), n.getMoneda(),
                    n.getSimbolo(), n.getIvaPorcentaje(), n.getMensajeTicket());
        }
    }

    private final NegocioRepository repo;

    @Transactional(readOnly = true)
    public Negocio actual() {
        long id = TenantContext.actual();
        return repo.findById(id).orElseThrow(() -> new NotFoundException("Negocio", id));
    }

    @Transactional(readOnly = true)
    public NegocioDto obtener() {
        return NegocioDto.from(actual());
    }

    @Transactional
    public NegocioDto actualizar(NegocioDto dto) {
        Negocio n = actual();
        n.setNombre(dto.nombre().trim());
        n.setNit(dto.nit());
        n.setDireccion(dto.direccion());
        n.setTelefono(dto.telefono());
        n.setMoneda(dto.moneda().toUpperCase());
        n.setSimbolo(dto.simbolo());
        n.setIvaPorcentaje(dto.ivaPorcentaje());
        n.setMensajeTicket(dto.mensajeTicket());
        return NegocioDto.from(repo.save(n));
    }
}
