import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { esNegocioSuspendido } from './auth/auth.interceptor';
import { ToastService } from './toast.service';

/** Permite que una llamada maneje sus propios errores: `{ context: new HttpContext().set(SILENT, true) }` */
export const SILENT = new HttpContextToken<boolean>(() => false);

/** Extrae el mensaje legible de un ProblemDetail del backend. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'No se pudo conectar con el servidor';
    const body = err.error as { detail?: string; errors?: Record<string, string> } | null;
    if (body?.errors) {
      return Object.entries(body.errors)
        .map(([campo, msg]) => `${campo}: ${msg}`)
        .join(' · ');
    }
    if (body?.detail) return body.detail;
    if (err.status === 403) return 'No tiene permisos para esta operación';
  }
  return 'Ocurrió un error inesperado';
}

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const toast = inject(ToastService);
  return next(req).pipe(
    catchError((err: unknown) => {
      const status = err instanceof HttpErrorResponse ? err.status : -1;
      // Las pantallas de login muestran su propio error
      const esLogin = req.url.endsWith('/auth/login');
      // Negocio suspendido: la sesión se cierra y el login muestra el motivo una sola vez
      const suspendido = err instanceof HttpErrorResponse && esNegocioSuspendido(err);
      if (!req.context.get(SILENT) && status !== 401 && !esLogin && !suspendido) {
        toast.error(errorMessage(err));
      }
      return throwError(() => err);
    }),
  );
};
