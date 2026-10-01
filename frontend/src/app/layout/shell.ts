import { CdkMenu, CdkMenuItem, CdkMenuTrigger } from '@angular/cdk/menu';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../core/auth/auth.service';
import { NegocioStore } from '../core/negocio.store';
import { Rol, ROLES } from '../core/models';
import { injectDialog, openDialog } from '../shared/ui/dialog';
import { PasswordDialog } from './password-dialog';

interface NavItem {
  path: string;
  label: string;
  icon: string;
  roles: Rol[];
}

const NAV: NavItem[] = [
  { path: '/dashboard', label: 'Inicio', icon: '◧', roles: ['ADMIN', 'CAJERO'] },
  { path: '/pos', label: 'Punto de venta', icon: '🛒', roles: ['ADMIN', 'CAJERO'] },
  { path: '/caja', label: 'Caja', icon: '💵', roles: ['ADMIN', 'CAJERO'] },
  { path: '/ventas', label: 'Ventas', icon: '🧾', roles: ['ADMIN', 'CAJERO'] },
  { path: '/clientes', label: 'Clientes', icon: '👥', roles: ['ADMIN', 'CAJERO'] },
  { path: '/catalogo', label: 'Catálogo', icon: '✂️', roles: ['ADMIN'] },
  { path: '/reportes', label: 'Reportes', icon: '📊', roles: ['ADMIN'] },
  { path: '/usuarios', label: 'Usuarios', icon: '🔑', roles: ['ADMIN'] },
  { path: '/configuracion', label: 'Configuración', icon: '⚙️', roles: ['ADMIN'] },
  { path: '/mis-comisiones', label: 'Mis comisiones', icon: '⭐', roles: ['ESTILISTA'] },
];

@Component({
  selector: 'sf-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CdkMenuTrigger, CdkMenu, CdkMenuItem],
  template: `
    <div class="flex h-full">
      <!-- Sidebar -->
      <aside
        class="fixed inset-y-0 left-0 z-30 w-60 -translate-x-full border-r border-slate-200 bg-white transition-transform lg:static lg:translate-x-0 print:hidden"
        [class.translate-x-0]="menuAbierto()"
      >
        <div class="flex h-16 items-center gap-2 border-b border-slate-200 px-5">
          <span
            class="flex size-8 items-center justify-center rounded-lg bg-brand-600 font-bold text-white"
            >S</span
          >
          <span class="truncate font-semibold text-slate-900">{{
            negocio.negocio()?.nombre ?? 'Stylo Flow'
          }}</span>
        </div>
        <nav class="flex flex-col gap-1 p-3">
          @for (item of nav(); track item.path) {
            <a
              [routerLink]="item.path"
              routerLinkActive="bg-brand-50 text-brand-700"
              class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
              (click)="menuAbierto.set(false)"
            >
              <span class="w-5 text-center" aria-hidden="true">{{ item.icon }}</span>
              {{ item.label }}
            </a>
          }
        </nav>
      </aside>

      @if (menuAbierto()) {
        <div
          class="fixed inset-0 z-20 bg-slate-900/40 lg:hidden"
          (click)="menuAbierto.set(false)"
        ></div>
      }

      <div class="flex min-w-0 flex-1 flex-col">
        <!-- Topbar -->
        <header
          class="flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 lg:px-6 print:hidden"
        >
          <button
            type="button"
            class="btn-ghost lg:hidden"
            (click)="menuAbierto.set(true)"
            aria-label="Abrir menú"
          >
            ☰
          </button>
          <div class="flex-1"></div>
          <button type="button" class="btn-ghost" [cdkMenuTriggerFor]="userMenu">
            <span
              class="flex size-8 items-center justify-center rounded-full bg-slate-200 text-sm font-semibold"
            >
              {{ iniciales() }}
            </span>
            <span class="hidden text-left sm:block">
              <span class="block text-sm font-medium text-slate-800">{{
                auth.usuario()?.nombre
              }}</span>
              <span class="block text-xs text-slate-500">{{ rolLabel() }}</span>
            </span>
          </button>
          <ng-template #userMenu>
            <div
              cdkMenu
              class="mt-1 w-48 rounded-lg border border-slate-200 bg-white py-1 shadow-lg"
            >
              <button
                cdkMenuItem
                class="w-full px-4 py-2 text-left text-sm hover:bg-slate-100"
                (cdkMenuItemTriggered)="cambiarPassword()"
              >
                Cambiar contraseña
              </button>
              <button
                cdkMenuItem
                class="w-full px-4 py-2 text-left text-sm text-red-600 hover:bg-slate-100"
                (cdkMenuItemTriggered)="auth.logout()"
              >
                Cerrar sesión
              </button>
            </div>
          </ng-template>
        </header>

        <main class="flex-1 overflow-auto p-4 lg:p-6">
          <router-outlet />
        </main>
      </div>
    </div>
  `,
})
export class Shell implements OnInit {
  protected readonly auth = inject(AuthService);
  protected readonly negocio = inject(NegocioStore);
  private readonly dialog = injectDialog();

  protected readonly menuAbierto = signal(false);
  protected readonly nav = computed(() => {
    const rol = this.auth.usuario()?.rol;
    return NAV.filter((i) => rol && i.roles.includes(rol));
  });
  protected readonly rolLabel = computed(
    () => ROLES.find((r) => r.value === this.auth.usuario()?.rol)?.label ?? '',
  );
  protected readonly iniciales = computed(() =>
    (this.auth.usuario()?.nombre ?? '?')
      .split(' ')
      .slice(0, 2)
      .map((p) => p[0]?.toUpperCase())
      .join(''),
  );

  ngOnInit(): void {
    this.negocio.cargar();
  }

  protected cambiarPassword(): void {
    openDialog(this.dialog, PasswordDialog, undefined, '24rem');
  }
}
