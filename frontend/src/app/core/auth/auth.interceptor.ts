import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';

/** Título del ProblemDetail que envía el backend cuando el negocio del token está suspendido. */
export const NEGOCIO_SUSPENDIDO = 'Business suspended';

/**
 * Agrega el JWT a las llamadas /api y cierra la sesión si el token ya no sirve: 401 (vencido o inválido)
 * o 403 de negocio suspendido.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const token = auth.token();
  const isApi = req.url.startsWith('/api');
  const authReq =
    token && isApi ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(authReq).pipe(
    catchError((err: unknown) => {
      if (err instanceof HttpErrorResponse && !req.url.endsWith('/auth/login')) {
        if (err.status === 401) {
          auth.logout();
        } else if (esNegocioSuspendido(err) && auth.isLoggedIn()) {
          auth.logout(true, 'Este negocio está suspendido. Contacte al soporte.');
        }
      }
      return throwError(() => err);
    }),
  );
};

export function esNegocioSuspendido(err: HttpErrorResponse): boolean {
  return (
    err.status === 403 && (err.error as { title?: string } | null)?.title === NEGOCIO_SUSPENDIDO
  );
}
