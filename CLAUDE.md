# Stylo Flow — guía para Claude

Sistema de gestión de peluquería: catálogo, usuarios, clientes, punto de venta (POS), caja y reportes.
Monorepo: `backend/` (Spring Boot 4.1, Java 17, Gradle) · `frontend/` (Angular 22, CDK, Tailwind v4) · PostgreSQL 16 en Docker.

## Comandos

| Qué | Comando |
|---|---|
| Base de datos | `docker compose up -d postgres` (puerto **5434**) |
| Backend | `cd backend && ./gradlew bootRun` → http://localhost:8085 (Swagger: `/swagger-ui.html`) |
| Tests backend | `cd backend && ./gradlew test` (Testcontainers: requiere Docker) |
| Frontend | `cd frontend && npm start` → http://localhost:4201 (proxy `/api` → 8085) |
| Tests frontend | `cd frontend && npx ng test --watch=false` (Vitest) |
| Build / tipos | `npm run build` · `npm run typecheck` · `npm run format` |

Usuario inicial: `admin` / `admin123` (lo crea `AdminInitializer` si no hay usuarios; configurable con `ADMIN_USERNAME`/`ADMIN_PASSWORD`).
Los puertos 5432/5433/8080/4200 están ocupados por otros proyectos en esta máquina; no los uses.

## Backend

- Paquetes por módulo en `com.styloflow.<modulo>`: `auth`, `usuarios`, `catalogo`, `clientes`, `caja`, `ventas`, `reportes`, `negocio`, `common`, `config`.
- DTOs como `record` dentro de `<Modulo>Dtos`; nunca exponer entidades JPA en controladores.
- Dinero siempre `BigDecimal` con escala 2 (`NUMERIC(12,2)`). Precios **incluyen IVA** (13 % por defecto, en tabla `negocio`).
- Errores: lanzar `NotFoundException` (404) o `BusinessException` (422); `GlobalExceptionHandler` los convierte en `ProblemDetail`.
- Seguridad: JWT HS256 con claims `uid` y `roles`. `CurrentUser.id()` da el usuario actual. Autorización con `@PreAuthorize` por rol (`ADMIN`, `CAJERO`, `ESTILISTA`).
- Fechas en `Instant`/`timestamptz`; "hoy" y los rangos de reportes se calculan con el bean `ZoneId` (`app.zona-horaria`, America/La_Paz).
- Esquema solo con Flyway (`ddl-auto: validate`). **Nunca edites una migración existente**: crea `V<n+1>__descripcion.sql` (un hook lo impide).
- Reportes con SQL nativo vía `JdbcClient` en `ReporteService`.
- Spring Boot 4 usa Jackson 3 (`tools.jackson.*`) y starters modulares (`spring-boot-starter-webmvc`, `-flyway`, etc.).

## Reglas de negocio clave

- Solo una caja abierta a la vez (índice único parcial). Sin caja abierta no se vende.
- Venta: precio de catálogo o manual por ítem; descuento global ≤ subtotal; en EFECTIVO se calcula el cambio; productos descuentan stock con bloqueo pesimista.
- Anular venta: solo ADMIN, solo si su caja sigue abierta; repone stock.
- Comisión del estilista = subtotal del servicio prorrateado por el descuento × `comision_porcentaje`.

## Frontend

- Componentes standalone con signals y control flow (`@if`, `@for`); plantillas inline. Sin NgModules ni zone.js.
- `core/api.service.ts` es el único cliente HTTP (un método por endpoint); tipos en `core/models.ts` reflejan los DTOs del backend — mantenlos sincronizados.
- `authInterceptor` añade el token y cierra sesión en 401; `errorInterceptor` muestra un toast con el `detail` del ProblemDetail (usa el `HttpContext` `SILENT` para manejar errores manualmente).
- Rutas lazy con `roleGuard(...)` en `app.routes.ts`; el menú lateral (`layout/shell.ts`) filtra por rol.
- UI: utilidades Tailwind propias en `styles.css` (`btn-primary`, `input`, `card`, `data-table`, …). Diálogos con CDK Dialog vía `openDialog()` en `shared/ui/dialog.ts`.
- Montos con el pipe `money` (usa el símbolo del negocio, `Bs`).

## Claude Code en este repo

- `.mcp.json`: `angular-cli` (docs y buenas prácticas de Angular) y `postgres` (crystaldba/postgres-mcp en Docker, **solo lectura**, contra la BD de desarrollo en `host.docker.internal:5434`; cambia la URL con `STYLO_MCP_DATABASE_URI`).
- Hooks (`.claude/hooks/*.mjs`): bloquean editar `.env*` y migraciones existentes; formatean con Prettier lo editado en `frontend/`; al terminar un turno compilan backend/frontend si hay cambios y devuelven los errores.
