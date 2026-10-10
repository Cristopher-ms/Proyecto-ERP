package com.erp.auth.config;

import com.erp.auth.domain.entity.User;
import com.erp.auth.domain.enums.Role;
import com.erp.auth.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Usuarios de demostración ya existentes en auth_db. Omitiendo carga inicial.");
            return;
        }

        log.info("Inicializando usuarios de demostración para el ERP...");

        // 1. Administrador general
        User admin = User.builder()
                .fullName("Cristopher Administrador")
                .email("admin@erp.com")
                .password(passwordEncoder.encode("Admin123!"))
                .role(Role.ROLE_ADMIN)
                .active(true)
                .build();
        userRepository.save(admin);

        // 2. Vendedor / Comercial
        User vendedor = User.builder()
                .fullName("Carlos Vendedor")
                .email("vendedor@erp.com")
                .password(passwordEncoder.encode("Vendedor123!"))
                .role(Role.ROLE_SELLER)
                .active(true)
                .build();
        userRepository.save(vendedor);

        // 3. Jefe de Almacén / Logística
        User almacen = User.builder()
                .fullName("Marcos Almacenero")
                .email("almacen@erp.com")
                .password(passwordEncoder.encode("Almacen123!"))
                .role(Role.ROLE_WAREHOUSE)
                .active(true)
                .build();
        userRepository.save(almacen);

        log.info("✅ 3 Usuarios semilla creados con éxito (admin@erp.com, vendedor@erp.com, almacen@erp.com).");
    }
}
