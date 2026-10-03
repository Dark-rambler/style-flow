import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginResponse, NegocioInfo, PlataformaLoginResponse, Rol, Usuario } from '../models';

/** v2: la sesión guarda `user`/`business` con los nombres del API en inglés. */
const STORAGE_KEY = 'stylo.session.v2';

interface Session {
  token: string;
  expiresAt: string;
  user: Usuario;
  /** Negocio de la sesión; ausente en sesiones de plataforma (superadmin). */
  business?: NegocioInfo;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(this.restore());

  readonly usuario = computed(() => this.session()?.user ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly token = computed(() => this.session()?.token ?? null);
  readonly negocio = computed(() => this.session()?.business ?? null);
  readonly esPlataforma = computed(() => this.usuario()?.role === 'SUPERADMIN');
  /** Motivo del último cierre de sesión forzado (p. ej. negocio suspendido); lo muestra el login. */
  readonly motivoCierre = signal<string | null>(null);

  /** Login dentro de un negocio: código del negocio + usuario + contraseña. */
  login(negocio: string, username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/auth/login', { businessCode: negocio, username, password })
      .pipe(
        tap((res) =>
          this.store({
            token: res.token,
            expiresAt: res.expiresAt,
            user: res.user,
            business: res.business,
          }),
        ),
      );
  }

  /** Login del superadmin de la plataforma (sin negocio). */
  loginPlataforma(username: string, password: string): Observable<PlataformaLoginResponse> {
    return this.http
      .post<PlataformaLoginResponse>('/api/platform/auth/login', { username, password })
      .pipe(
        tap((res) =>
          this.store({
            token: res.token,
            expiresAt: res.expiresAt,
            user: {
              id: 0,
              name: res.name,
              username,
              role: 'SUPERADMIN',
              phone: null,
              commissionRate: 0,
              active: true,
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
    const rol = this.usuario()?.role;
    return !!rol && roles.includes(rol);
  }

  /** Página inicial según el rol. */
  homePath(): string {
    if (this.hasRole('SUPERADMIN')) return '/plataforma';
    return this.hasRole('STYLIST') ? '/mis-comisiones' : '/dashboard';
  }

  private store(s: Session): void {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(s));
    this.session.set(s);
  }

  private restore(): Session | null {
    try {
      // Sesión con el formato anterior (campos en español): ya no sirve
      localStorage.removeItem('stylo.session');
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
