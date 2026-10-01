import { HttpContextToken, HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { ToastService } from './toast.service';

/** Permite que una llamada maneje sus propios errores: `{ context: new HttpContext().set(SILENT, true) }` */
export const SILENT = new HttpContextToken<boolean>(() => false);

/** Extrae el mensaje legible de un ProblemDetail del backend. */
export function errorMessage(err: unknown): string {
  if (err instanceof HttpErrorResponse) {
    if (err.status === 0) return 'No se pudo conectar con el servidor';
    const body = err.error as { detail?: string; errores?: Record<string, string> } | null;
    if (body?.errores) {
      return Object.entries(body.errores)
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
      if (!req.context.get(SILENT) && status !== 401) {
        toast.error(errorMessage(err));
      }
      return throwError(() => err);
    }),
  );
};
