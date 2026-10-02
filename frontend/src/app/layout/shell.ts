import { CdkMenu, CdkMenuItem, CdkMenuTrigger } from '@angular/cdk/menu';
import { Component, computed, effect, inject, OnInit, signal } from '@angular/core';
import { toSignal } from '@angular/core/rxjs-interop';
import { NavigationEnd, Router, RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { filter, map } from 'rxjs';
import { AuthService } from '../core/auth/auth.service';
import { CajaStore } from '../core/caja.store';
import { NegocioStore } from '../core/negocio.store';
import { Rol, ROLES } from '../core/models';
import { injectDialog, openDialog } from '../shared/ui/dialog';
import { Icon, IconName } from '../shared/ui/icon';
import { PasswordDialog } from './password-dialog';

interface NavItem {
  path: string;
  label: string;
  icon: IconName;
  roles: Rol[];
}

const SIDEBAR_KEY = 'stylo.sidebar';

const NAV: NavItem[] = [
  { path: '/dashboard', label: 'Inicio', icon: 'dashboard', roles: ['ADMIN', 'CAJERO'] },
  { path: '/cobrar', label: 'Cobrar', icon: 'cart', roles: ['ADMIN', 'CAJERO'] },
  { path: '/caja', label: 'Caja', icon: 'wallet', roles: ['ADMIN', 'CAJERO'] },
  { path: '/ventas', label: 'Historial de ventas', icon: 'receipt', roles: ['ADMIN', 'CAJERO'] },
  { path: '/clientes', label: 'Clientes', icon: 'users', roles: ['ADMIN', 'CAJERO'] },
  { path: '/catalogo', label: 'Catálogo', icon: 'scissors', roles: ['ADMIN'] },
  { path: '/reportes', label: 'Reportes', icon: 'chart', roles: ['ADMIN'] },
  { path: '/usuarios', label: 'Usuarios', icon: 'shield', roles: ['ADMIN'] },
  { path: '/configuracion', label: 'Configuración', icon: 'settings', roles: ['ADMIN'] },
  { path: '/mis-comisiones', label: 'Mis comisiones', icon: 'percent', roles: ['ESTILISTA'] },
  { path: '/plataforma', label: 'Negocios', icon: 'building', roles: ['SUPERADMIN'] },
];

@Component({
  selector: 'sf-shell',
  imports: [RouterOutlet, RouterLink, RouterLinkActive, CdkMenuTrigger, CdkMenu, CdkMenuItem, Icon],
  template: `
    <div class="flex h-full">
      <!-- Sidebar -->
      <aside
        class="fixed inset-y-0 left-0 z-30 flex w-60 shrink-0 -translate-x-full flex-col border-r border-slate-200 bg-white transition-[translate,width] lg:relative lg:translate-x-0 print:hidden"
        [class.translate-x-0]="menuAbierto()"
        [class]="rail() ? 'lg:w-16' : ''"
      >
        <div
          class="flex h-16 shrink-0 items-center gap-2 border-b border-slate-200 px-5"
          [class]="rail() ? 'lg:justify-center lg:px-0' : ''"
        >
          @if (auth.esPlataforma()) {
            <img src="/image/logoNC-icono.png" alt="Nexus Corp" class="size-8 shrink-0" />
          } @else {
            <img src="/image/StyloFlow-icono.png" alt="Stylo Flow" class="size-8 shrink-0" />
          }
          <span class="min-w-0 leading-tight" [class]="rail() ? 'lg:hidden' : ''">
            <span class="block truncate font-semibold text-slate-900">{{
              auth.esPlataforma()
                ? 'Plataforma'
                : (negocio.negocio()?.nombre ?? auth.negocio()?.nombre ?? 'Stylo Flow')
            }}</span>
            @if (auth.negocio(); as n) {
              <span class="block truncate text-xs text-slate-400">{{ n.codigo }}</span>
            }
          </span>
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
              <sf-icon [name]="item.icon" class="size-5" />
              <span class="truncate" [class]="rail() ? 'lg:sr-only' : ''">{{ item.label }}</span>
            </a>
          }
        </nav>
        <!-- Marca del desarrollador: logo completo, o solo el ícono con el menú contraído -->
        <div
          class="flex shrink-0 items-center justify-center border-t border-slate-200 px-5 py-3"
          [class]="rail() ? 'lg:px-0' : ''"
        >
          @if (rail()) {
            <img
              src="/image/logoNC-icono.png"
              alt="Nexus Corp"
              title="Desarrollado por Nexus Corp"
              class="hidden size-7 lg:block"
            />
          }
          <span class="flex flex-col items-center gap-1" [class]="rail() ? 'lg:hidden' : ''">
            <span class="text-[10px] tracking-wide text-slate-400 uppercase">Desarrollado por</span>
            <img src="/image/logoNC-claro.png" alt="Nexus Corp" class="h-6 w-auto" />
          </span>
        </div>
        <!-- Botón en el borde del menú (escritorio): contraer / expandir -->
        <button
          type="button"
          class="absolute top-20 -right-3 z-10 hidden size-6 items-center justify-center rounded-full border border-slate-300 bg-white text-slate-500 shadow-sm transition hover:border-brand-300 hover:text-brand-600 focus-visible:outline-2 focus-visible:outline-brand-500 lg:flex"
          [attr.aria-label]="rail() ? 'Expandir menú' : 'Contraer menú'"
          [attr.title]="rail() ? 'Expandir menú' : 'Contraer menú'"
          [attr.aria-expanded]="!rail()"
          (click)="alternarMenu()"
        >
          <sf-icon [name]="rail() ? 'chevronRight' : 'chevronLeft'" class="size-4" />
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
          <!-- Mostrar/ocultar menú: en escritorio alterna entre completo e íconos; en móvil lo abre encima -->
          <!-- <button
            type="button"
            class="btn-ghost -ml-2 px-2 text-slate-500 hover:text-slate-800"
            aria-label="Mostrar u ocultar menú"
            [attr.title]="rail() ? 'Expandir menú' : 'Contraer menú'"
            (click)="alternarMenu()"
          >
            <sf-icon name="panel" class="size-5" />
          </button> -->
          @if (auth.hasRole('ADMIN', 'CAJERO') && cajaStore.cargada()) {
            <a
              routerLink="/caja"
              class="ml-2 inline-flex items-center gap-2 rounded-full border px-3 py-1 text-xs font-medium transition"
              [class]="
                cajaStore.abierta()
                  ? 'border-emerald-200 bg-emerald-50 text-emerald-700 hover:bg-emerald-100'
                  : 'border-amber-200 bg-amber-50 text-amber-700 hover:bg-amber-100'
              "
              [attr.title]="
                cajaStore.abierta() ? 'Ver la caja del turno' : 'Abrir caja para poder cobrar'
              "
            >
              <span
                class="size-2 rounded-full"
                [class]="cajaStore.abierta() ? 'bg-emerald-500' : 'bg-amber-500'"
                aria-hidden="true"
              ></span>
              {{ cajaStore.abierta() ? 'Caja abierta' : 'Caja cerrada' }}
            </a>
          }
          <div class="flex-1"></div>
          <button type="button" class="btn-ghost cursor-pointer" [cdkMenuTriggerFor]="userMenu">
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
                class="flex w-full items-center cursor-pointer gap-2 px-4 py-2 text-left text-sm hover:bg-slate-100"
                (cdkMenuItemTriggered)="cambiarPassword()"
              >
                <sf-icon name="key" class="size-4 text-slate-400" />
                Cambiar contraseña
              </button>
              <button
                cdkMenuItem
                class="flex w-full items-center cursor-pointer gap-2 px-4 py-2 text-left text-sm text-red-600 hover:bg-slate-100"
                (cdkMenuItemTriggered)="auth.logout()"
              >
                <sf-icon name="logout" class="size-4" />
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
  protected readonly cajaStore = inject(CajaStore);
  private readonly dialog = injectDialog();

  private readonly router = inject(Router);

  protected readonly menuAbierto = signal(false);

  /** Preferencia del usuario fuera del POS (persistida). */
  private readonly colapsado = signal(leerPreferencia());
  /** En Cobrar el menú se contrae solo; el usuario puede expandirlo mientras siga ahí. */
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
  protected readonly rolLabel = computed(() =>
    this.auth.esPlataforma()
      ? 'Superadministrador'
      : (ROLES.find((r) => r.value === this.auth.usuario()?.rol)?.label ?? ''),
  );
  protected readonly iniciales = computed(() =>
    (this.auth.usuario()?.nombre ?? '?')
      .split(' ')
      .slice(0, 2)
      .map((p) => p[0]?.toUpperCase())
      .join(''),
  );

  ngOnInit(): void {
    // El superadmin no pertenece a ningún negocio: no hay configuración ni caja que cargar
    if (!this.auth.esPlataforma()) this.negocio.cargar();
    if (this.auth.hasRole('ADMIN', 'CAJERO')) this.cajaStore.refrescar();
  }

  protected alternarMenu(): void {
    if (matchMedia('(min-width: 64rem)').matches) {
      this.alternarSidebar();
    } else {
      this.menuAbierto.update((v) => !v);
    }
  }

  private alternarSidebar(): void {
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
    return this.router.url.startsWith('/cobrar');
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
