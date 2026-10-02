# Stylo Flow — Backend

API REST del SaaS multitenant de gestión de peluquerías: catálogo, usuarios, clientes, cobro (POS), caja y reportes por negocio, más un panel de plataforma para el superadmin.

Spring Boot 4 · Java 26 · Gradle · PostgreSQL 16 · Flyway · JWT · arquitectura hexagonal por módulo.

## Requisitos

- JDK 26
- Docker (para PostgreSQL)

## Cómo levantarlo

1. Variables de entorno (desde `backend/`):

   ```bash
   cp .env.example .env
   ```

   El backend lee `backend/.env` automáticamente (valores de desarrollo local).

2. Base de datos (desde la raíz del repo):

   ```bash
   docker compose up -d postgres
   ```

3. Backend (desde `backend/`):

   ```bash
   ./gradlew bootRun
   ```

   Flyway crea el esquema al arrancar.

- API: http://localhost:8080
- Swagger: http://localhost:8080/swagger-ui.html

## Accesos de desarrollo

| Tipo | Negocio | Usuario | Contraseña |
|---|---|---|---|
| Negocio | `demo` | `admin` | `admin123` |
| Plataforma | — | `superadmin` | `superadmin123` |

## Build

```bash
./gradlew build
```
