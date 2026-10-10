# 📋 Registro de Cambios y Documentación Técnica - Fase 1 (MVP 1)
**Proyecto:** ERP Empresarial en Microservicios  
**Fase:** Fase 1 / MVP 1 - Core & Catálogo de Inventario  
**Infraestructura:** Microsoft Azure Cloud (En Vivo)  
**Fecha de Actualización:** Octubre 2026  

---

## 1. Resumen Ejecutivo

Durante la **Fase 1**, se ha construido y desplegado en la nube de **Microsoft Azure** la base fundacional del ERP empresarial. El sistema cuenta con:
* **Backend de microservicios**: Desarrollado en **Java 21 LTS** con **Spring Boot 3.3.4**, Spring Data JPA, Hibernate, OpenAPI/Swagger 3 y Spring Boot Actuator.
* **Frontend SPA corporativo**: Desarrollado en **Angular 18** utilizando componentes modernos *Standalone*, Signals, diseño responsivo empresarial y servicios desacoplados con `HttpClient`.
* **Persistencia en la nube**: Base de datos relacional **PostgreSQL 16 Flexible Server** en Azure (`erp_inventory_db`) con tablas normalizadas, índices optimizados y carga inicial de datos.
* **Orquestación en la nube**: Microservicio empaquetado en contenedor Docker con Eclipse Temurin 21 JRE Alpine, alojado en **Azure Container Registry (ACR)** y ejecutado en **Azure Container Apps (ACA)**.
* **Distribución Web**: Frontend alojado en **Azure Static Web Apps (SWA)** con CI/CD automatizado mediante **GitHub Actions**.

---

## 2. Arquitectura de Despliegue en Microsoft Azure

```
+-----------------------------------------------------------------------------------+
|                            MICROSOFT AZURE CLOUD                                  |
|                                                                                   |
|  +-------------------------------------+      HTTPS / REST (JSON)                 |
|  |       Azure Static Web Apps         | ----------------------------+            |
|  |  (agreeable-mud-0875d3610.6...)     |                             |            |
|  |  Angular 18 SPA Frontend            |                             v            |
|  +-------------------------------------+              +-----------------------+   |
|                                                       | Azure Container Apps  |   |
|                                                       | Ingress Proxy (Envoy) |   |
|  +-------------------------------------+              | CORS Policy / Edge    |   |
|  |      Azure Container Registry       |              +-----------------------+   |
|  |     (acrerpcristopher.azurecr.io)   |                         |                |
|  |  Docker Images (inventory-service)  |                         v                |
|  +-------------------------------------+              +-----------------------+   |
|                     | Pull Image                      |   inventory-service   |   |
|                     +-------------------------------->| Java 21 / Spring Boot |   |
|                                                       | Puerto interno: 8082  |   |
|                                                       +-----------------------+   |
|                                                                  |                |
|                                                       JDBC 5432  | (SSL Require)  |
|                                                                  v                |
|                                                       +-----------------------+   |
|                                                       | Azure Database for    |   |
|                                                       | PostgreSQL Flexible   |   |
|                                                       | (erp-db-cristopher)   |   |
|                                                       +-----------------------+   |
+-----------------------------------------------------------------------------------+
```

---

## 3. Bitácora de Cambios Técnicos e Incidencias Resueltas

### 3.1. Enrutamiento SPA en Azure Static Web Apps (Error 404 al recargar)
* **Incidencia:** Al navegar o refrescar directamente en subrutas de Angular como `/inventory` o `/categories`, Azure Static Web Apps devolvía `HTTP 404 Not Found`.
* **Causa Raíz:** En aplicaciones SPA (*Single Page Application*), el servidor web debe redirigir todas las solicitudes de rutas no estáticas hacia `index.html` para que el enrutador de Angular gestione la vista en el cliente.
* **Solución Implementada:** Se configuró el archivo `public/staticwebapp.config.json` con `navigationFallback` hacia `/index.html` y código de estado 200.

### 3.2. Error HTTP 400 "Invalid Argument" en Spring Boot por conflicto CORS
* **Incidencia:** Peticiones `curl` y llamadas HTTP entre frontend y backend fallaban con:
  ```json
  {"status":400,"error":"Invalid Argument","message":"When allowCredentials is true, allowedOrigins cannot contain the special value \"*\"..."}
  ```
* **Causa Raíz:** Según la especificación oficial W3C/RFC de CORS, cuando se habilitan credenciales (`allowCredentials: true`), está prohibido devolver el comodín literal `*` en `Access-Control-Allow-Origin`. Spring MVC valida esto internamente y arroja `IllegalArgumentException` si los controladores tienen `@CrossOrigin(origins = "*")`.
* **Solución Implementada:** Se refactorizaron los controladores `CategoryController`, `ProductController` y `StockController` cambiando `origins = "*"` por `originPatterns = "*"`. Con este atributo, Spring Boot evalúa dinámicamente el origen entrante y devuelve el origen específico de Azure Static Web Apps en el encabezado.

### 3.3. Bloqueo de peticiones POST / Fallo de Preflight OPTIONS ("NetworkError when attempting to fetch resource")
* **Incidencia:** El frontend carga exitosamente los datos mediante peticiones `GET` (productos, KPIs, selector de categorías), pero al intentar crear una categoría o registrar un producto (`POST`), el navegador arroja:
  `NetworkError when attempting to fetch resource`.
* **Causa Raíz:**
  1. Las peticiones `GET` son peticiones simples (*Simple Requests*) que no envían cabeceras complejas y no activan preflight.
  2. Las peticiones `POST` envían el encabezado `Content-Type: application/json`, lo que obliga al navegador a enviar primero una petición de verificación `OPTIONS` (*CORS Preflight*).
  3. En la configuración de Azure Container Apps Ingress se tenía `--allowed-headers "*"` con `--allow-credentials true`. Según la especificación oficial de Fetch y MDN:
     > *Cuando `allowCredentials` es `true`, el comodín `*` en `Access-Control-Allow-Headers` NO es tratado como comodín, sino como el texto literal `"*"`.*
     Al no coincidir literalmente `Content-Type` con `*`, el navegador rechaza el preflight y cancela la petición `POST` antes de enviarla.
* **Solución:** Declarar explícitamente en el Ingress de Azure Container Apps las cabeceras permitidas:
  `Content-Type`, `Authorization`, `Accept`, `Origin`, `X-Requested-With`.

---

## 4. Endpoints Expuestos y Disponibles

| Método | Endpoint | Descripción | Estado |
|---|---|---|---|
| `GET` | `/api/v1/categories` | Lista todas las categorías activas | Activo en Producción |
| `GET` | `/api/v1/categories/all` | Lista todas las categorías (incluye inactivas) | Activo en Producción |
| `POST` | `/api/v1/categories` | Crea una nueva categoría con validación DTO | Activo en Backend |
| `PUT` | `/api/v1/categories/{id}` | Actualiza el nombre/descripción de una categoría | Activo en Backend |
| `DELETE` | `/api/v1/categories/{id}` | Desactivación lógica de una categoría | Activo en Backend |
| `GET` | `/api/v1/products` | Consulta paginada con filtros por nombre/SKU/stock | Activo en Producción |
| `GET` | `/api/v1/products/summary` | KPIs cuantitativos y valorización en dólares | Activo en Producción |
| `POST` | `/api/v1/products` | Registro de producto con auditoría de stock inicial | Activo en Backend |
| `POST` | `/api/v1/inventory/adjust` | Ajuste de inventario (Entrada, Salida, Calibración) | Activo en Backend |
| `GET` | `/v3/api-docs` | Especificación OpenAPI 3.0 en formato JSON | Activo |
| `GET` | `/actuator/health` | Sonda de salud y conectividad a base de datos | Activo |

---

## 5. Próximos Pasos (Transición a Fase 2)
1. **Microservicio de Autenticación (`auth-service`)**: Spring Security 6, JWT, encriptación BCrypt, roles (`ADMIN`, `ALMACENERO`, `VENDEDOR`).
2. **API Gateway (`api-gateway`)**: Spring Cloud Gateway para centralizar el enrutamiento, CORS unificado y validación de tokens Bearer.
3. **Módulo de Login en Angular**: Interceptor HTTP para inyección automática de tokens JWT.
