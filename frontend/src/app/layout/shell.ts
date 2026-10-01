import { CdkMenu, CdkMenuItem, CdkMenuTrigger } from '@angular/cdk/menu';
import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
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

const SIDEBAR_KEY = 'stylo.sidebar';

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
        class="fixed inset-y-0 left-0 z-30 flex w-60 shrink-0 -translate-x-full flex-col border-r border-slate-200 bg-white transition-[translate,width] lg:static lg:translate-x-0 print:hidden"
        [class.translate-x-0]="menuAbierto()"
        [class]="rail() ? 'lg:w-16' : ''"
      >
        <div
          class="flex h-16 shrink-0 items-center gap-2 border-b border-slate-200 px-5"
          [class]="rail() ? 'lg:justify-center lg:px-0' : ''"
        >
          <span
            class="flex size-8 shrink-0 items-center justify-center rounded-lg bg-brand-600 font-bold text-white"
            >S</span
          >
          <span class="truncate font-semibold text-slate-900" [class]="rail() ? 'lg:hidden' : ''">{{
            negocio.negocio()?.nombre ?? 'Stylo Flow'
          }}</span>
        </div>
        <nav
          class="flex flex-1 flex-col gap-1 overflow-y-auto p-3"
          [class]="rail() ? 'lg:px-2' : ''"
        >
          @for (item of nav(); track item.path) {
            <a
              [routerLink]="item.path"
              routerLinkActive="bg-brand-50 text-brand-700"
              class="flex items-center gap-3 rounded-lg px-3 py-2 text-sm font-medium text-slate-600 hover:bg-slate-100"
              [class]="rail() ? 'lg:justify-center lg:px-0' : ''"
              [attr.title]="rail() ? item.label : null"
              (click)="menuAbierto.set(false)"
            >
              <span class="w-5 shrink-0 text-center" aria-hidden="true">{{ item.icon }}</span>
              <span class="truncate" [class]="rail() ? 'lg:sr-only' : ''">{{ item.label }}</span>
            </a>
          }
        </nav>
        <button
          type="button"
          class="hidden h-11 shrink-0 items-center gap-3 border-t border-slate-200 px-6 text-sm text-slate-500 hover:bg-slate-50 hover:text-slate-800 lg:flex"
          [class]="rail() ? 'lg:justify-center lg:px-0' : ''"
          [attr.aria-expanded]="!rail()"
          [attr.title]="rail() ? 'Expandir menú' : 'Contraer menú'"
          (click)="alternarSidebar()"
        >
          <span aria-hidden="true" class="text-base">{{ rail() ? '»' : '«' }}</span>
          <span [class]="rail() ? 'lg:sr-only' : ''">{{
            rail() ? 'Expandir menú' : 'Contraer menú'
          }}</span>
        </button>
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

  private readonly router = inject(Router);

  protected readonly menuAbierto = signal(false);

  /** Preferencia del usuario fuera del POS (persistida). */
  private readonly colapsado = signal(leerPreferencia());
  /** En el POS el menú se contrae solo; el usuario puede expandirlo mientras siga ahí. */
  private readonly expandidoEnPos = signal(false);
  private readonly enPos = toSignal(
    this.router.events.pipe(
      filter((e) => e instanceof NavigationEnd),
      map(() => this.esPos()),
    ),
    { initialValue: this.esPos() },
  );
  /** Sidebar reducido a íconos (solo aplica en pantallas lg+). */
  protected readonly rail = computed(() =>
    this.enPos() ? !this.expandidoEnPos() : this.colapsado(),
  );

  constructor() {
    // Al salir del POS se olvida la expansión manual: la próxima visita vuelve a contraerse.
    effect(() => {
      if (!this.enPos()) this.expandidoEnPos.set(false);
    });
  }

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

  protected alternarSidebar(): void {
    if (this.enPos()) {
      this.expandidoEnPos.update((v) => !v);
      return;
    }
    const valor = !this.colapsado();
    this.colapsado.set(valor);
    try {
      localStorage.setItem(SIDEBAR_KEY, valor ? 'rail' : 'abierto');
    } catch {
      // almacenamiento no disponible: la preferencia dura solo esta sesión
    }
  }

  private esPos(): boolean {
    return this.router.url.startsWith('/pos');
  }

  protected cambiarPassword(): void {
    openDialog(this.dialog, PasswordDialog, undefined, '24rem');
  }
}

function leerPreferencia(): boolean {
  try {
    return localStorage.getItem(SIDEBAR_KEY) === 'rail';
  } catch {
    return false;
  }
}
