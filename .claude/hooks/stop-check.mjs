// Stop: si Claude dejó cambios sin commitear en backend/ o frontend/, verifica que compilen.
// Si hay errores, bloquea el fin del turno y se los devuelve a Claude para que los corrija.
// Solo vuelve a compilar cuando los archivos cambiaron desde la última verificación.
import { spawnSync } from 'node:child_process';
import { createHash } from 'node:crypto';
import { existsSync, mkdirSync, readFileSync, statSync, writeFileSync } from 'node:fs';
import { join } from 'node:path';

const input = JSON.parse(readFileSync(0, 'utf8') || '{}');
if (input.stop_hook_active) process.exit(0); // evita bucles: ya se bloqueó una vez

const root = process.env.CLAUDE_PROJECT_DIR ?? process.cwd();
const isWin = process.platform === 'win32';

const status = spawnSync('git', ['status', '--porcelain', '--untracked-files=all'], { cwd: root, encoding: 'utf8' });
const changed = (status.stdout ?? '')
  .split('\n')
  .map((l) => l.slice(3).trim().replace(/^"|"$/g, ''))
  .filter(Boolean);

const back = changed.filter((f) => /^backend\/src\/.+\.(java|ya?ml|sql|properties)$/.test(f) || /^backend\/.*\.gradle\.kts$/.test(f));
const front = changed.filter((f) => /^frontend\/src\/.+\.(ts|html)$/.test(f));
if (!back.length && !front.length) process.exit(0);

// Huella de los archivos cambiados (ruta + mtime) para no recompilar si nada cambió.
const hash = createHash('sha1');
for (const f of [...back, ...front]) {
  const p = join(root, f);
  hash.update(f + (existsSync(p) ? statSync(p).mtimeMs : 'deleted'));
}
const fingerprint = hash.digest('hex');
const cacheDir = join(root, '.claude', '.cache');
const cacheFile = join(cacheDir, 'stop-check');
if (existsSync(cacheFile) && readFileSync(cacheFile, 'utf8') === fingerprint) process.exit(0);

const errors = [];
const run = (cmd, args, cwd) =>
  spawnSync(cmd, args, { cwd, encoding: 'utf8', shell: isWin, timeout: 240_000 });

if (back.length) {
  const r = run(isWin ? 'gradlew.bat' : './gradlew', ['compileJava', 'compileTestJava', '-q', '--console=plain'], join(root, 'backend'));
  if (r.status !== 0) errors.push('Backend (gradlew compileJava):\n' + tail(r.stdout + r.stderr));
}
if (front.length) {
  const r = run('npx', ['tsc', '-p', 'tsconfig.app.json', '--noEmit'], join(root, 'frontend'));
  if (r.status !== 0) errors.push('Frontend (tsc --noEmit):\n' + tail(r.stdout + r.stderr));
}

if (errors.length) {
  process.stdout.write(
    JSON.stringify({ decision: 'block', reason: 'El código no compila. Corrija estos errores:\n\n' + errors.join('\n\n') }),
  );
} else {
  mkdirSync(cacheDir, { recursive: true });
  writeFileSync(cacheFile, fingerprint);
}

function tail(text) {
  return text.trim().split('\n').slice(-40).join('\n');
}
