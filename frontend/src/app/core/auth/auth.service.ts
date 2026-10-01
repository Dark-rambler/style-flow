import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginResponse, NegocioInfo, PlataformaLoginResponse, Rol, Usuario } from '../models';

const STORAGE_KEY = 'stylo.session';

interface Session {
  token: string;
  expiresAt: string;
  usuario: Usuario;
  /** Negocio de la sesión; ausente en sesiones de plataforma (superadmin). */
  negocio?: NegocioInfo;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(this.restore());

  readonly usuario = computed(() => this.session()?.usuario ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly token = computed(() => this.session()?.token ?? null);
  readonly negocio = computed(() => this.session()?.negocio ?? null);
  readonly esPlataforma = computed(() => this.usuario()?.rol === 'SUPERADMIN');
  /** Motivo del último cierre de sesión forzado (p. ej. negocio suspendido); lo muestra el login. */
  readonly motivoCierre = signal<string | null>(null);

  /** Login dentro de un negocio: código del negocio + usuario + contraseña. */
  login(negocio: string, username: string, password: string): Observable<LoginResponse> {
    return this.http.post<LoginResponse>('/api/auth/login', { negocio, username, password }).pipe(
      tap((res) =>
        this.store({
          token: res.token,
          expiresAt: res.expiresAt,
          usuario: res.usuario,
          negocio: res.negocio,
        }),
      ),
    );
  }

  /** Login del superadmin de la plataforma (sin negocio). */
  loginPlataforma(username: string, password: string): Observable<PlataformaLoginResponse> {
    return this.http
      .post<PlataformaLoginResponse>('/api/plataforma/auth/login', { username, password })
      .pipe(
        tap((res) =>
          this.store({
            token: res.token,
            expiresAt: res.expiresAt,
            usuario: {
              id: 0,
              nombre: res.nombre,
              username,
              rol: 'SUPERADMIN',
              telefono: null,
              comisionPorcentaje: 0,
              activo: true,
            },
          }),
        ),
      );
  }

  logout(redirect = true, motivo: string | null = null): void {
    const destino = this.esPlataforma() ? '/plataforma/login' : '/login';
    this.motivoCierre.set(motivo);
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
    if (redirect) {
      this.router.navigate([destino]);
    }
  }

  hasRole(...roles: Rol[]): boolean {
    const rol = this.usuario()?.rol;
    return !!rol && roles.includes(rol);
  }

  /** Página inicial según el rol. */
  homePath(): string {
    if (this.hasRole('SUPERADMIN')) return '/plataforma';
    return this.hasRole('ESTILISTA') ? '/mis-comisiones' : '/dashboard';
  }

  private store(s: Session): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
    this.session.set(s);
  }

  private restore(): Session | null {
    try {
      const raw = localStorage.getItem(STORAGE_KEY);
      if (!raw) return null;
      const s = JSON.parse(raw) as Session;
      if (new Date(s.expiresAt).getTime() <= Date.now()) {
        localStorage.removeItem(STORAGE_KEY);
        return null;
      }
      return s;
    } catch {
      return null;
    }
  }
}
