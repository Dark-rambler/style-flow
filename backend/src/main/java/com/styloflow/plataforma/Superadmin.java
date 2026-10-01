package com.styloflow.plataforma;

import com.styloflow.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/** Usuario de la plataforma: da de alta y suspende negocios. No pertenece a ningún negocio. */
@Getter
@Setter
@Entity
@Table(name = "superadmins")
public class Superadmin extends BaseEntity {

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(nullable = false)
    private boolean activo = true;
}
