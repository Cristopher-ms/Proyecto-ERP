package com.erp.auth.domain.enums;

import lombok.Getter;

@Getter
public enum Role {
    ROLE_ADMIN("Administrador", "Acceso total al sistema y gestión de usuarios"),
    ROLE_SELLER("Vendedor", "Gestión de pedidos, clientes y cotizaciones"),
    ROLE_WAREHOUSE("Almacenero", "Gestión de catálogo, existencias y movimientos de almacén");

    private final String displayName;
    private final String description;

    Role(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
