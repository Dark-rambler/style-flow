package com.styloflow.clientes;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class ClienteDtos {

    private ClienteDtos() {}

    public record ClienteRequest(
            @NotBlank @Size(max = 120) String nombre,
            @Size(max = 30) String telefono,
            @Email @Size(max = 120) String email,
            @Size(max = 30) String ciNit,
            @Size(max = 500) String notas) {}

    public record ClienteResponse(Long id, String nombre, String telefono, String email, String ciNit, String notas,
            Instant createdAt) {

        static ClienteResponse from(Cliente c) {
            return new ClienteResponse(c.getId(), c.getNombre(), c.getTelefono(), c.getEmail(), c.getCiNit(),
                    c.getNotas(), c.getCreatedAt());
        }
    }
}
