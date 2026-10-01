import { HttpClient } from '@angular/common/http';
import { computed, inject, Injectable, signal } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { LoginResponse, Rol, Usuario } from '../models';

const STORAGE_KEY = 'stylo.session';

interface Session {
  token: string;
  expiresAt: string;
  usuario: Usuario;
}

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly session = signal<Session | null>(this.restore());

  readonly usuario = computed(() => this.session()?.usuario ?? null);
  readonly isLoggedIn = computed(() => this.session() !== null);
  readonly token = computed(() => this.session()?.token ?? null);

  login(username: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>('/api/auth/login', { username, password })
      .pipe(tap((res) => this.store(res)));
  }

  logout(redirect = true): void {
    localStorage.removeItem(STORAGE_KEY);
    this.session.set(null);
    if (redirect) {
      this.router.navigate(['/login']);
    }
  }

  hasRole(...roles: Rol[]): boolean {
    const rol = this.usuario()?.rol;
    return !!rol && roles.includes(rol);
  }

  /** Página inicial según el rol. */
  homePath(): string {
    return this.hasRole('ESTILISTA') ? '/mis-comisiones' : '/dashboard';
  }

  private store(res: LoginResponse): void {
    const s: Session = { token: res.token, expiresAt: res.expiresAt, usuario: res.usuario };
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
