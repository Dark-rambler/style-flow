package com.styloflow.usuarios;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public final class UsuarioDtos {

    private UsuarioDtos() {}

    public record UsuarioRequest(
            @NotBlank @Size(max = 120) String nombre,
            @NotBlank @Size(min = 3, max = 50) @Pattern(regexp = "^[a-zA-Z0-9._-]+$",
                    message = "solo letras, números, punto, guion y guion bajo") String username,
            @Size(min = 6, max = 72) String password,
            @NotNull Rol rol,
            @Size(max = 30) String telefono,
            @DecimalMin("0") @DecimalMax("100") BigDecimal comisionPorcentaje,
            Boolean activo) {}

    public record CambiarPasswordRequest(@NotBlank @Size(min = 6, max = 72) String password) {}

    public record UsuarioResponse(Long id, String nombre, String username, Rol rol, String telefono,
            BigDecimal comisionPorcentaje, boolean activo) {

        public static UsuarioResponse from(Usuario u) {
            return new UsuarioResponse(u.getId(), u.getNombre(), u.getUsername(), u.getRol(), u.getTelefono(),
                    u.getComisionPorcentaje(), u.isActivo());
        }
    }

    /** Vista reducida para selectores (p. ej. elegir estilista en el POS). */
    public record UsuarioResumen(Long id, String nombre, Rol rol) {

        public static UsuarioResumen from(Usuario u) {
            return new UsuarioResumen(u.getId(), u.getNombre(), u.getRol());
        }
    }
}
