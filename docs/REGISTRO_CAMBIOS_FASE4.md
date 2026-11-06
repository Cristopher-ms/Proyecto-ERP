# Registro de Cambios — Fase 4: Módulo de Compras, Directorio de Proveedores y Resiliencia

## Resumen Ejecutivo
Se implementó el microservicio **`purchases-service`** (Puerto 8084), completando el flujo B2B de aprovisionamiento de stock, gestión integral de proveedores y compras corporativas con tolerancia a fallos.

---

## 1. Arquitectura del Microservicio `purchases-service`

- **Runtime & Framework**: Java 21 LTS + Spring Boot 3.3.4
- **Persistencia**: Spring Data JPA + Hibernate contra PostgreSQL Flexible Server en Azure (`erp_purchases_db`).
- **Resiliencia & Circuit Breaker**:
  - `Resilience4j 2.2.0` (Circuit Breaker con ventana deslizante de 10 llamadas y tolerancia al 50% de fallos, Retry exponencial con backoff x2).
  - Fallback no bloqueante si `inventory-service` entra en arranque en frío.
- **Comunicación entre Microservicios**:
  - `WebClient` para reposición sincrónica directa de stock (`POST /api/v1/products/{id}/add-stock`).
  - `Spring AMQP / RabbitMQ` para eventos asíncronos (`StockReplenishEvent` hacia exchange `erp.purchase.exchange`).
  - Modo simulación resiliente (`erp.rabbitmq.enabled=false`) para ejecución desacoplada sin RabbitMQ activo en nube.
- **Seguridad & CORS**: Configurado con `allowedOriginPatterns: "*"` y credenciales habilitadas.

---

## 2. Componentes Backend Creados

### `backend/purchases-service/`
- **Dominio**:
  - `Supplier`: Entidad de proveedor con RUC/TaxID único, términos de pago (`PaymentTerms`), país y estado.
  - `PurchaseOrder`: Orden de compra con correlativo mensual (`PO-YYYYMM-XXXX`), cálculo de IGV (18%), desglose y estados (`DRAFT` → `APPROVED` → `RECEIVED`).
  - `PurchaseItem`: Líneas de detalle de la orden de compra con subtotales automáticos.
- **Servicios & Lógica de Negocio**:
  - `SupplierService`: CRUD, búsqueda por nombre o RUC.
  - `PurchaseOrderService`: Ciclo de vida completo (crear, aprobar, recibir reponiendo stock, KPIs y resumen).
  - `InventoryServiceClient`: Cliente HTTP reactivo con `@CircuitBreaker` y `@Retry` resilientes.
  - `PurchaseEventPublisher`: Emisor de eventos orientado a arquitectura EDA.
- **Controladores REST**:
  - `SupplierController` (`/api/v1/suppliers`): Gestión de proveedores con OpenAPI / Swagger.
  - `PurchaseController` (`/api/v1/purchases`): Operaciones sobre órdenes de compra y resumen financiero.
- **Infraestructura**:
  - `DataInitializer`: Carga automática de 4 proveedores semilla y 2 órdenes de compra de prueba.
  - `Dockerfile`: Multi-stage con Maven 3.9.8 + Eclipse Temurin 21 Alpine con usuario de seguridad no privilegiado `erpuser`.
  - `.github/workflows/deploy-purchases-service.yml`: CI/CD para compilación y push a `acrerpcristopher.azurecr.io/purchases-service:latest`.

### `backend/inventory-service/`
- Agregado DTO `AddStockRequest`.
- Agregado método `addStock()` en `ProductService` registrando movimiento `MovementType.IN` en el Kardex.
- Agregado endpoint `POST /api/v1/products/{id}/add-stock` en `ProductController`.
- Agregada prueba unitaria en `ProductServiceTest`.

---

## 3. Frontend Angular 18 (`erp-web`)

- **Modelos**: `purchase.model.ts` con interfaces completas y tipos TypeScript estrictos.
- **Servicios Reactivos**: `purchase.service.ts` con Angular Signals, `timeout(5000)` y mock fallback corporativo que garantiza 100% de disponibilidad ante arranques en frío.
- **Componente**: `purchases.component` con:
  - 4 KPIs financieros en tiempo real (Total OCs, Total Compras S/, Pendientes, Compras del Mes).
  - Pestaña de Órdenes de Compra con estados y acciones (Aprobar y Recibir con actualización de stock).
  - Pestaña de Directorio de Proveedores con buscador en vivo por RUC o Razón Social.
  - Pestaña de Formulario para registro de Nueva Orden de Compra.
- **Enrutamiento & Navegación**:
  - Ruta `/purchases` protegida con `authGuard`.
  - Enlace en la barra lateral con badge "En Vivo".
