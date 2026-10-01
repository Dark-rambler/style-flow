// PreToolUse (Edit|Write): bloquea cambios en archivos sensibles.
//  - .env / .env.* (excepto .env.example): contienen secretos.
//  - Migraciones Flyway ya existentes: editarlas rompe el checksum en bases ya migradas;
//    los cambios de esquema van en una migración nueva (V<n+1>__descripcion.sql).
import { existsSync, readFileSync } from 'node:fs';
import { basename } from 'node:path';

const input = JSON.parse(readFileSync(0, 'utf8') || '{}');
const filePath = String(input.tool_input?.file_path ?? '');
const name = basename(filePath);
const normalized = filePath.replaceAll('\\', '/');

let reason = null;
if (/^\.env(\..+)?$/.test(name) && name !== '.env.example') {
  reason = `No se permite editar ${name}: contiene secretos. Edite .env.example o pida al usuario que lo cambie.`;
} else if (/\/db\/migration\/V\d+__.+\.sql$/.test(normalized) && existsSync(filePath)) {
  reason =
    `La migración ${name} ya existe y puede estar aplicada. ` +
    'No la modifique: cree una nueva migración Flyway con el siguiente número de versión.';
}

if (reason) {
  process.stdout.write(
    JSON.stringify({
      hookSpecificOutput: {
        hookEventName: 'PreToolUse',
        permissionDecision: 'deny',
        permissionDecisionReason: reason,
      },
    }),
  );
}
