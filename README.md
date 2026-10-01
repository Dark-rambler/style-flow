# Stylo Flow

Plataforma **multitenant** de gestión para peluquerías. Cada peluquería (negocio) trabaja con sus datos aislados:

- **Cobrar** — punto de venta con servicios, productos, estilista por servicio, descuentos, cortesías y ticket imprimible.
- **Caja** — apertura y cierre de turno con arqueo de efectivo.
- **Catálogo**, **clientes** (con historial de compras) y **usuarios** con roles.
- **Reportes** — ventas por día, por método de pago, comisiones por estilista, más vendidos y exportación CSV.
- **Plataforma** — un superadmin da de alta, suspende y reactiva peluquerías.

| Capa | Tecnología |
|---|---|
| Frontend | Angular 22 · Angular CDK · Tailwind CSS 4 · Chart.js |
| Backend | Spring Boot 4.1 · Java 17 · Gradle · Spring Security (JWT) · Flyway |
| Base de datos | PostgreSQL 16 (Docker) |

---

## 1. Requisitos

| Herramienta | Versión | Verificar |
|---|---|---|
| Git | cualquiera reciente | `git --version` |
| Java (JDK) | 17 o superior | `java -version` |
| Node.js | 22 o superior (incluye npm 10) | `node -v` · `npm -v` |
| Docker Desktop | encendido | `docker info` |

No hace falta instalar Gradle, Maven, Angular CLI ni PostgreSQL: el proyecto trae el wrapper de Gradle, el CLI de
Angular se instala con `npm install` y PostgreSQL corre en Docker.

---

## 2. Instalación paso a paso

### 2.1 Clonar el repositorio

```bash
git clone -b dev https://github.com/Dark-rambler/style-flow.git
cd style-flow
```

### 2.2 Variables de entorno (opcional en desarrollo)

Todo funciona con los valores por defecto. Para personalizarlos, copia la plantilla:

```bash
# Linux / macOS
cp .env.example .env
# Windows (PowerShell)
Copy-Item .env.example .env
```

| Variable | Para qué sirve | Por defecto |
|---|---|---|
| `POSTGRES_DB`, `POSTGRES_USER`, `POSTGRES_PASSWORD` | Base y credenciales que crea Docker | `stylo` / `stylo` / `stylo` |
| `POSTGRES_PORT` | Puerto de PostgreSQL en tu equipo | `5434` |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | Conexión del backend (el puerto debe coincidir con `POSTGRES_PORT`) | `jdbc:postgresql://localhost:5434/stylo` |
| `JWT_SECRET` | Clave para firmar las sesiones (mínimo 32 caracteres) | clave de desarrollo |
| `JWT_EXPIRATION_HOURS` | Duración de la sesión | `8` |
| `SERVER_PORT` | Puerto de la API | `8085` |
| `CORS_ORIGINS` | Orígenes permitidos (admite patrones) | `http://localhost:*` |
| `ADMIN_USERNAME`, `ADMIN_PASSWORD` | Admin del negocio `demo` (se crea si no tiene usuarios) | `admin` / `admin123` |
| `SUPERADMIN_USERNAME`, `SUPERADMIN_PASSWORD` | Superadmin de la plataforma (se crea si no existe) | `superadmin` / `superadmin123` |

Docker Compose y el backend leen el mismo `.env` de la raíz.

### 2.3 Base de datos

```bash
docker compose up -d postgres
```

Queda lista cuando `docker compose ps` muestra `stylo-postgres` como **healthy**. Las tablas se crean solas al arrancar
el backend (migraciones Flyway).

> pgAdmin opcional: `docker compose --profile tools up -d pgadmin` → http://localhost:5050 (`admin@stylo.local` / `admin`).

### 2.4 Backend (API)

```bash
cd backend
# Windows (PowerShell)
.\gradlew.bat bootRun
# Linux / macOS
./gradlew bootRun
```

La primera vez descarga dependencias (unos minutos). Está listo cuando aparece:

```
Started StyloFlowApplication in ... seconds
```

API en http://localhost:8085 · documentación Swagger en http://localhost:8085/swagger-ui.html
(usa el token de `POST /api/auth/login` en *Authorize*).

### 2.5 Frontend

En **otra terminal**:

```bash
cd frontend
npm install
npm start
```

Está listo cuando aparece `Local: http://localhost:4201/`. Abre esa dirección en el navegador.
En desarrollo, las llamadas a `/api` se redirigen al backend (`frontend/proxy.conf.json`).

---

## 3. Puertos

| Servicio | Puerto | Dónde se cambia |
|---|---|---|
| PostgreSQL | 5434 | `POSTGRES_PORT` y `DB_URL` en `.env` |
| API (Spring Boot) | 8085 | `SERVER_PORT` en `.env` **y** `target` en `frontend/proxy.conf.json` |
| App (Angular) | 4201 | `serve.options.port` en `frontend/angular.json` |
| pgAdmin (opcional) | 5050 | `docker-compose.yml` |

Se usan puertos poco habituales para no chocar con otros proyectos (5432, 8080, 4200).

---

## 4. Primer ingreso

| Acceso | Dirección | Negocio | Usuario | Contraseña |
|---|---|---|---|---|
| Peluquería de ejemplo | http://localhost:4201/login | `demo` | `admin` | `admin123` |
| Plataforma (superadmin) | http://localhost:4201/plataforma/login | — | `superadmin` | `superadmin123` |

El negocio `demo` trae un catálogo de ejemplo. **Cambia ambas contraseñas** fuera de desarrollo
(`ADMIN_PASSWORD`, `SUPERADMIN_PASSWORD`, o desde *menú de usuario → Cambiar contraseña*).

### Dar de alta una peluquería

1. Entra a la **plataforma** como superadmin.
2. *Negocios → + Nuevo negocio*: nombre, **código de acceso** (p. ej. `salon-bella`), administrador inicial y,
   si quieres, el catálogo base de servicios.
3. Comparte con la peluquería su código, usuario y contraseña. Sus usuarios ingresan en `/login` con esos tres datos.

Desde el mismo panel puedes **suspender** un negocio (sus usuarios quedan fuera de inmediato) y **reactivarlo**.

### Primeros pasos dentro de un negocio

1. **Configuración** → nombre, NIT, dirección y mensaje del ticket.
2. **Usuarios** → crea cajeros y estilistas (con su % de comisión).
3. **Catálogo** → ajusta servicios, precios y productos con su stock.
4. **Caja** → abre la caja con el fondo inicial (también se puede abrir desde *Cobrar*).
5. **Cobrar** → registra ventas e imprime el ticket. Al final del día, cierra la caja en **Caja**.

---

## 5. Roles

| Rol | Puede |
|---|---|
| ADMIN | Todo dentro de su negocio: catálogo, usuarios, reportes, configuración, anular ventas |
| CAJERO | Cobrar, caja, historial de ventas y clientes |
| ESTILISTA | Ver sus servicios realizados y sus comisiones |
| SUPERADMIN | Plataforma: alta, suspensión y métricas de negocios (no opera dentro de ellos) |

---

## 6. Tests

```bash
# Backend: integración con PostgreSQL real vía Testcontainers (Docker debe estar encendido)
cd backend
./gradlew test            # Windows: .\gradlew.bat test

# Frontend: Vitest
cd frontend
npx ng test --watch=false
npm run typecheck
```

Los tests del backend incluyen el flujo completo de venta y caja, y el **aislamiento entre negocios**
(`MultiTenantIsolationTest`).

---

## 7. Estructura del repositorio

```
style-flow/
├─ backend/                      Spring Boot
│  └─ src/main/
│     ├─ java/com/styloflow/
│     │  ├─ auth/  usuarios/  catalogo/  clientes/  caja/  ventas/  reportes/  negocio/
│     │  ├─ plataforma/          superadmin y alta de negocios
│     │  ├─ tenant/              contexto multitenant (Hibernate @TenantId)
│     │  └─ common/  config/
│     └─ resources/db/migration/ migraciones Flyway (V1…V5)
├─ frontend/                     Angular
│  └─ src/app/
│     ├─ core/                   API, autenticación, interceptores, stores
│     ├─ features/               pantallas (pos, caja, ventas, clientes, catalogo, reportes, plataforma…)
│     ├─ layout/                 menú lateral y barra superior
│     └─ shared/                 pipes y componentes de UI
├─ docker-compose.yml            PostgreSQL (+ pgAdmin opcional)
├─ .env.example                  plantilla de variables
├─ .mcp.json  .claude/           configuración de Claude Code
└─ CLAUDE.md                     convenciones del proyecto
```

---

## 8. Desarrollo con Claude Code

El repositorio incluye configuración para [Claude Code](https://claude.com/claude-code):

- **`.mcp.json`** — servidores MCP: `angular-cli` (documentación y buenas prácticas de Angular) y `postgres`
  (consultas **de solo lectura** a la base de desarrollo, vía Docker). Claude Code pide aprobarlos la primera vez.
- **Hooks** (`.claude/settings.json`): impiden editar `.env` y migraciones ya aplicadas, formatean con Prettier lo
  editado en `frontend/` y, al terminar cada respuesta, verifican que backend y frontend compilen.
- **`CLAUDE.md`** — reglas del proyecto (multitenant, dinero en `BigDecimal`, DTOs, etc.).

---

## 9. Producción

Variables **obligatorias**:

| Variable | Valor |
|---|---|
| `JWT_SECRET` | clave aleatoria de 32+ caracteres |
| `SUPERADMIN_PASSWORD`, `ADMIN_PASSWORD` | contraseñas seguras |
| `DB_URL`, `DB_USER`, `DB_PASSWORD` | base de datos de producción |
| `CORS_ORIGINS` | dominio real, p. ej. `https://app.mipeluqueria.com` |

Construcción:

```bash
cd backend && ./gradlew bootJar          # genera backend/build/libs/backend-0.0.1-SNAPSHOT.jar
java -jar build/libs/backend-0.0.1-SNAPSHOT.jar

cd frontend && npm run build             # genera frontend/dist/frontend/browser
```

- Sirve `frontend/dist/frontend/browser` y la API bajo el **mismo dominio** (un proxy inverso que envíe `/api` al backend),
  o ajusta `CORS_ORIGINS`.
- Desactiva Swagger: `springdoc.api-docs.enabled=false` y `springdoc.swagger-ui.enabled=false`.
- El estado "negocio suspendido" se cachea en memoria: con **varias instancias** del backend, cambia la caché a Redis.

---

## 10. Solución de problemas

| Síntoma | Solución |
|---|---|
| `Bind for 0.0.0.0:5434 failed: port is already allocated` | Otro servicio usa el puerto: cambia `POSTGRES_PORT` y el puerto de `DB_URL` en `.env`. |
| `Port 8085 was already in use` | Ya hay un backend corriendo (¿otra terminal?) o cambia `SERVER_PORT` y `frontend/proxy.conf.json`. |
| Angular arranca en un puerto distinto a 4201 | El 4201 estaba ocupado; funciona igual (CORS acepta cualquier puerto de localhost). |
| Login: "Negocio, usuario o contraseña incorrectos" | Revisa el **código de negocio** (`demo` en desarrollo) además del usuario. |
| Login: "Este negocio está suspendido" | Reactívalo desde la plataforma (superadmin). |
| `Connection refused` a PostgreSQL al iniciar el backend | Docker Desktop apagado o el contenedor no está *healthy*: `docker compose up -d postgres`. |
| Tests del backend fallan con `Could not find a valid Docker environment` | Testcontainers necesita Docker encendido. |
| `./gradlew: Permission denied` (Linux/macOS) | `chmod +x backend/gradlew` |
| Cambios de base de datos no aparecen | Reinicia el backend: Flyway aplica las migraciones nuevas al arrancar. |
| Error 403 en todas las llamadas tras cambiar de negocio | Cierra sesión y vuelve a entrar con el código correcto. |
