# Registro de Cambios - Fase 3: Ventas, Pedidos y Event-Driven con RabbitMQ

**Proyecto**: ERP Cloud Enterprise  
**Fecha de Implementación**: 2026-11-02  
**Estado**: Completado en código y preparado para despliegue en Microsoft Azure Cloud  

---

## 1. Resumen Ejecutivo de la Fase 3

En esta fase se implementó el módulo troncal de **Ventas, Gestión de Clientes Corporativos y Facturación de Pedidos (`orders-service`)**, adoptando un patrón de **Arquitectura Dirigida por Eventos (Event-Driven)**:

1. **Desacoplamiento de Servicios**: Al concretarse un pedido, se emite un evento asíncrono (`OrderCreatedEvent`) hacia el broker de mensajería (RabbitMQ / Event Stream), eliminando el bloqueo HTTP hacia `inventory-service`.
2. **Kardex y Descuento Automático de Stock**: `inventory-service` procesa la reducción de existencias y registra el movimiento de almacén con referencia al número de pedido correlativo (`ORD-YYYYMM-XXXX`).
3. **Punto de Venta Reactivo (POS)** en Angular 18: Interfaz ágil con selector dinámico de clientes, catálogo interactivo con validación de stock disponible, carrito de compras con cálculo de impuestos (IGV 18%) y confirmación en tiempo real.

---

## 2. Componentes Técnicos Implementados

### Backend: Microservicio `orders-service` (Java 21 / Spring Boot 3.3.4)
* **Entidades JPA**:
  * [`Customer`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/backend/orders-service/src/main/java/com/erp/orders/domain/entity/Customer.java): Directorio de clientes con validación de DNI/RUC.
  * [`Order`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/backend/orders-service/src/main/java/com/erp/orders/domain/entity/Order.java): Pedido maestro con número correlativo único, totales y estados.
  * [`OrderItem`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/backend/orders-service/src/main/java/com/erp/orders/domain/entity/OrderItem.java): Líneas de venta con snapshot de precio y subtotal.
* **Arquitectura de Mensajería**:
  * [`RabbitMQConfig.java`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/backend/orders-service/src/main/java/com/erp/orders/config/RabbitMQConfig.java): Declaración de `TopicExchange` (`erp.order.exchange`) y cola persistente.
  * [`OrderEventPublisher.java`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/backend/orders-service/src/main/java/com/erp/orders/event/OrderEventPublisher.java): Publicación de eventos con fallback resiliente.
* **Controladores y Endpoints**:
  * `GET /api/v1/customers`: Búsqueda de clientes por razón social o documento.
  * `POST /api/v1/customers`: Registro rápido de clientes.
  * `GET /api/v1/orders`: Listado paginado con filtros por estado (`CONFIRMED`, `PENDING`, `CANCELLED`).
  * `POST /api/v1/orders`: Emisión de pedido y disparo de evento.
  * `GET /api/v1/orders/summary`: KPIs de facturación (Ventas totales, ticket promedio, ventas diarias).
* **Infraestructura**:
  * Dockerfile multi-stage basado en Eclipse Temurin 21 Alpine con usuario sin privilegios.
  * Workflow en GitHub Actions: [`.github/workflows/deploy-orders-service.yml`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/.github/workflows/deploy-orders-service.yml).

### Integración en `inventory-service`
* Endpoint de salida de existencias: `POST /api/v1/products/{id}/deduct-stock`.
* Registro automático de trazabilidad en el Kardex (`StockMovement` tipo `OUT` con motivo `Venta de Pedido ORD-XXXX`).

### Frontend: Módulo de Ventas en Angular 18 (`erp-web`)
* **Punto de Venta POS**:
  * [`orders.component.ts`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/frontend/erp-web/src/app/features/orders/orders.component.ts)
  * [`orders.component.html`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/frontend/erp-web/src/app/features/orders/orders.component.html)
  * [`orders.component.css`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/frontend/erp-web/src/app/features/orders/orders.component.css)
* **Servicio Reactivo**:
  * [`order.service.ts`](file:///C:/Users/Billy/.gemini/antigravity/scratch/erp-system/frontend/erp-web/src/app/core/services/order.service.ts) con Signals y mock fallback para reclutadores.
* **Ruta y Navegación**:
  * Ruta protegida `/orders` habilitada en `app.routes.ts`.
  * Enlace activo "Ventas & Pedidos" en la barra lateral con distintivo *En Vivo*.

---

## 3. Guía de Despliegue en Microsoft Azure

1. **Crear base de datos en PostgreSQL Flexible Server**:
   ```bash
   az postgres flexible-server db create \
     --resource-group ERP-PORTAFOLIO \
     --server-name erp-db-cristopher \
     --name erp_orders_db
   ```
2. **Subir los cambios al repositorio**:
   ```bash
   git add .
   git commit -m "feat(fase-3): implementar orders-service, POS reactivo y arquitectura asincrona"
   git push origin main
   ```
3. **Crear el Contenedor en Azure Container Apps**:
   ```bash
   az containerapp create \
     --name app-orders-service \
     --resource-group ERP-PORTAFOLIO \
     --environment env-erp-prod \
     --image acrerpcristopher.azurecr.io/orders-service:latest \
     --registry-server acrerpcristopher.azurecr.io \
     --target-port 8083 \
     --ingress external \
     --min-replicas 1 \
     --max-replicas 2 \
     --cpu 0.5 \
     --memory 1.0Gi \
     --env-vars \
       SPRING_DATASOURCE_URL="jdbc:postgresql://erp-db-cristopher.postgres.database.azure.com:5432/erp_orders_db?sslmode=require" \
       SPRING_DATASOURCE_USERNAME="erp_admin" \
       SPRING_DATASOURCE_PASSWORD="<TU_CONTRASEÑA_DE_POSTGRES>" \
       HIBERNATE_DDL_AUTO="update"
   ```
