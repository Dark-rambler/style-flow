import { provideHttpClient } from '@angular/common/http';
import { TestBed } from '@angular/core/testing';
import { provideRouter, Router, UrlTree } from '@angular/router';
import { beforeEach, describe, expect, it } from 'vitest';
import { roleGuard } from './auth.guards';
import { AuthService } from './auth.service';

function sesion(rol: string): void {
  localStorage.setItem(
    'stylo.session.v2',
    JSON.stringify({
      token: 't',
      expiresAt: new Date(Date.now() + 3_600_000).toISOString(),
      user: {
        id: 1,
        name: 'X',
        username: 'x',
        role: rol,
        phone: null,
        commissionRate: 0,
        active: true,
      },
    }),
  );
}

describe('roleGuard', () => {
  beforeEach(() => localStorage.clear());

  const setup = () => {
    TestBed.configureTestingModule({ providers: [provideRouter([]), provideHttpClient()] });
    return TestBed.inject(Router);
  };

  it('permite el acceso con el rol correcto', () => {
    sesion('ADMIN');
    setup();
    const result = TestBed.runInInjectionContext(() =>
      roleGuard('ADMIN')({} as never, {} as never),
    );
    expect(result).toBe(true);
  });

  it('redirige a la página del rol cuando no tiene permiso', () => {
    sesion('STYLIST');
    const router = setup();
    const result = TestBed.runInInjectionContext(() =>
      roleGuard('ADMIN')({} as never, {} as never),
    );
    expect(router.serializeUrl(result as UrlTree)).toBe('/mis-comisiones');
  });

  it('el superadmin no entra a vistas de negocio y va al panel de plataforma', () => {
    sesion('SUPERADMIN');
    const router = setup();
    const result = TestBed.runInInjectionContext(() =>
      roleGuard('ADMIN', 'CASHIER')({} as never, {} as never),
    );
    expect(router.serializeUrl(result as UrlTree)).toBe('/plataforma');
  });

  it('descarta una sesión expirada', () => {
    localStorage.setItem(
      'stylo.session.v2',
      JSON.stringify({ token: 't', expiresAt: '2000-01-01T00:00:00Z', user: {} }),
    );
    setup();
    expect(TestBed.inject(AuthService).isLoggedIn()).toBe(false);
  });
});
