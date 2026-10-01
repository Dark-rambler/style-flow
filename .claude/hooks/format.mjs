// PostToolUse (Edit|Write): formatea con Prettier los archivos del frontend recién editados.
import { spawnSync } from 'node:child_process';
import { existsSync, readFileSync } from 'node:fs';
import { join, relative } from 'node:path';

const input = JSON.parse(readFileSync(0, 'utf8') || '{}');
const filePath = String(input.tool_response?.filePath ?? input.tool_input?.file_path ?? '');
const root = process.env.CLAUDE_PROJECT_DIR ?? process.cwd();
const frontend = join(root, 'frontend');
const rel = relative(frontend, filePath);
const prettier = join(frontend, 'node_modules', 'prettier', 'bin', 'prettier.cjs');

const isFrontendFile = rel && !rel.startsWith('..') && !rel.includes('node_modules');
if (isFrontendFile && /\.(ts|html|css|scss|json)$/.test(filePath) && existsSync(prettier)) {
  spawnSync(process.execPath, [prettier, '--write', '--log-level', 'warn', filePath], {
    cwd: frontend,
    stdio: 'ignore',
  });
}
