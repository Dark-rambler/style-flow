import { Routes } from '@angular/router';
import { authGuard, guestGuard, roleGuard } from './core/auth/auth.guards';
import { Shell } from './layout/shell';

export const routes: Routes = [
  {
    path: 'login',
    canActivate: [guestGuard],
    loadComponent: () => import('./features/login/login.page').then((m) => m.LoginPage),
  },
  {
    path: 'plataforma/login',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./features/plataforma/plataforma-login.page').then((m) => m.PlataformaLoginPage),
  },
  {
    path: '',
    component: Shell,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        canActivate: [roleGuard('ADMIN', 'CASHIER')],
        loadComponent: () =>
          import('./features/dashboard/dashboard.page').then((m) => m.DashboardPage),
      },
      {
        path: 'cobrar',
        canActivate: [roleGuard('ADMIN', 'CASHIER')],
        loadComponent: () => import('./features/pos/pos.page').then((m) => m.PosPage),
      },
      {
        path: 'caja',
        canActivate: [roleGuard('ADMIN', 'CASHIER')],
        loadComponent: () => import('./features/caja/caja.page').then((m) => m.CajaPage),
      },
      {
        path: 'ventas',
        canActivate: [roleGuard('ADMIN', 'CASHIER')],
        loadComponent: () => import('./features/ventas/ventas.page').then((m) => m.VentasPage),
      },
      {
        path: 'clientes',
        canActivate: [roleGuard('ADMIN', 'CASHIER')],
        loadComponent: () =>
          import('./features/clientes/clientes.page').then((m) => m.ClientesPage),
      },
      {
        path: 'catalogo',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/catalogo/catalogo.page').then((m) => m.CatalogoPage),
      },
      {
        path: 'reportes',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/reportes/reportes.page').then((m) => m.ReportesPage),
      },
      {
        path: 'usuarios',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/usuarios/usuarios.page').then((m) => m.UsuariosPage),
      },
      {
        path: 'configuracion',
        canActivate: [roleGuard('ADMIN')],
        loadComponent: () =>
          import('./features/configuracion/configuracion.page').then((m) => m.ConfiguracionPage),
      },
      {
        path: 'mis-comisiones',
        loadComponent: () =>
          import('./features/mis-comisiones/mis-comisiones.page').then((m) => m.MisComisionesPage),
      },
      {
        path: 'plataforma',
        canActivate: [roleGuard('SUPERADMIN')],
        loadComponent: () =>
          import('./features/plataforma/negocios.page').then((m) => m.NegociosPage),
      },
      { path: 'pos', redirectTo: 'cobrar' },
      { path: '', pathMatch: 'full', redirectTo: 'dashboard' },
    ],
  },
  { path: '**', redirectTo: '' },
];
