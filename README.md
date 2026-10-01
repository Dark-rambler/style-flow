# Stylo Flow

Gestión para peluquerías: catálogo de servicios y productos, usuarios con roles, clientes, punto de venta con ticket, caja con arqueo y reportes (ventas, métodos de pago, comisiones por estilista, más vendidos, export CSV).

**Stack:** Angular 22 + Angular CDK + Tailwind CSS 4 · Spring Boot 4.1 (Java 17, Gradle) · PostgreSQL 16 · JWT.

## Requisitos

- Java 17+, Node 22+, Docker Desktop.

## Puesta en marcha

```bash
cp .env.example .env              # opcional: ajustar credenciales y JWT_SECRET
docker compose up -d postgres     # PostgreSQL en localhost:5434

cd backend && ./gradlew bootRun   # API en http://localhost:8085  (Windows: gradlew.bat bootRun)
cd frontend && npm install && npm start   # App en http://localhost:4201
```

Ingresa con **admin / admin123** y cambia la contraseña desde el menú de usuario.

Primeros pasos en la app:
1. **Configuración** → datos del negocio (NIT, dirección, mensaje del ticket).
2. **Usuarios** → crea cajeros y estilistas (con su % de comisión).
3. **Catálogo** → ajusta servicios, precios y productos.
4. **Caja** → abre la caja con el fondo inicial (también se puede abrir desde **Cobrar**).
5. **Cobrar** → registra la venta e imprime el ticket; al cerrar el día, arquea la caja en **Caja**.

## Roles

| Rol | Puede |
|---|---|
| ADMIN | Todo: catálogo, usuarios, reportes, configuración, anular ventas |
| CAJERO | Cobrar, caja, historial de ventas, clientes |
| ESTILISTA | Ver sus servicios y comisiones |

## Documentación de la API

Swagger UI: http://localhost:8085/swagger-ui.html (usa el token de `POST /api/auth/login` en *Authorize*).

## Tests

```bash
cd backend && ./gradlew test               # integración con Testcontainers (requiere Docker)
cd frontend && npx ng test --watch=false   # Vitest
```

## Producción (notas)

- Define `JWT_SECRET` (≥ 32 caracteres), `ADMIN_PASSWORD`, credenciales de BD y `CORS_ORIGINS`.
- `npm run build` genera `frontend/dist/frontend/browser`; sírvelo detrás del mismo dominio que la API (o ajusta CORS).
- Desactiva Swagger con `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false`.
