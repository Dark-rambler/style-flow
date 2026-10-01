# Stylo Flow — Frontend

Aplicación Angular 22 (componentes standalone, signals, Angular CDK y Tailwind CSS 4).
La guía de instalación completa está en el [README principal](../README.md).

## Comandos

| Comando | Qué hace |
|---|---|
| `npm install` | Instala dependencias |
| `npm start` | Servidor de desarrollo en http://localhost:4201 (proxy `/api` → http://localhost:8085) |
| `npm run build` | Build de producción en `dist/frontend/browser` |
| `npx ng test --watch=false` | Tests unitarios (Vitest) |
| `npm run typecheck` | Verificación de tipos |
| `npm run format` | Formatea con Prettier |

El backend debe estar corriendo para usar la app (ver README principal). Convenciones del código en [`../CLAUDE.md`](../CLAUDE.md).
