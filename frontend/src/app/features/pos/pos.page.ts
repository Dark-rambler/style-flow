import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { ApiService } from '../../core/api.service';
import {
  Caja,
  Cliente,
  Producto,
  Servicio,
  TipoItem,
  UsuarioResumen,
  VentaRequest,
} from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { MoneyPipe } from '../../shared/money.pipe';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { ClienteFormDialog } from '../clientes/cliente-form.dialog';
import { TicketDialog } from '../ventas/ticket.dialog';
import { CobroDialog, CobroData, CobroResult } from './cobro.dialog';
import { ItemDetalleData, ItemDetalleDialog } from './item-detalle.dialog';

interface CartLine {
  key: number;
  tipo: TipoItem;
  itemId: number;
  nombre: string;
  precio: number;
  cantidad: number;
  /** Descuento en monto sobre la línea (precio × cantidad). */
  descuento: number;
  estilistaId: number | null;
  stock?: number;
}

/** Ítem del catálogo tal como se muestra en la grilla del POS. */
interface ItemPos {
  tipo: TipoItem;
  id: number;
  codigo: string;
  nombre: string;
  precio: number;
  categoria: string;
  agotado: boolean;
  ref: Servicio | Producto;
}

const GENERAL = '__general__';
const PRODUCTOS = '__productos__';

@Component({
  selector: 'sf-pos-page',
  imports: [FormsModule, MoneyPipe, RouterLink],
  host: { '(window:keydown.f4)': 'atajoCobrar($event)' },
  template: `
    <!-- El layout responde al ancho disponible (container queries), no al viewport:
         así se adapta tanto con el menú lateral abierto como contraído. -->
    <div class="@container">
      <div class="flex flex-col gap-3 @4xl:h-[calc(100vh-7rem)]">
        @if (cajaCargada() && !caja()) {
          <div
            class="flex shrink-0 items-center justify-between rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-800"
          >
            <span>No hay una caja abierta. Debe abrir caja para registrar ventas.</span>
            <a routerLink="/caja" class="btn-primary btn-sm">Abrir caja</a>
          </div>
        }

        <div
          class="grid min-h-0 flex-1 gap-3 @4xl:grid-cols-[minmax(0,1fr)_minmax(26rem,32rem)] @6xl:grid-cols-[12rem_minmax(0,1fr)_minmax(28rem,36rem)]"
        >
          <!-- Categorías -->
          <nav
            class="card hidden min-h-0 flex-col overflow-hidden @6xl:flex"
            aria-label="Categorías"
          >
            <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
              <svg
                class="size-4 shrink-0 text-slate-400"
                viewBox="0 0 24 24"
                fill="none"
                stroke="currentColor"
                stroke-width="2"
              >
                <circle cx="11" cy="11" r="7" />
                <path d="m20 20-3.5-3.5" />
              </svg>
              <input
                class="w-full bg-transparent py-1 text-sm outline-none placeholder:text-slate-400"
                placeholder="Buscar categoría…"
                [ngModel]="qCategoria()"
                (ngModelChange)="qCategoria.set($event)"
              />
            </div>
            <ul class="flex-1 overflow-auto">
              @for (c of categoriasVisibles(); track c.id) {
                <li>
                  <button
                    type="button"
                    class="flex w-full items-center justify-between border-b border-slate-100 px-4 py-3 text-left text-sm uppercase transition hover:bg-slate-50"
                    [class]="
                      categoria() === c.id
                        ? 'bg-brand-50 font-semibold text-brand-700'
                        : 'text-slate-600'
                    "
                    [attr.aria-current]="categoria() === c.id"
                    (click)="categoria.set(c.id)"
                  >
                    {{ c.nombre }}
                    @if (categoria() === c.id) {
                      <span aria-hidden="true">›</span>
                    }
                  </button>
                </li>
              }
            </ul>
          </nav>

          <!-- Catálogo -->
          <section class="flex min-h-0 flex-col gap-3">
            <div
              class="-mx-1 flex gap-2 overflow-x-auto px-1 pb-1 @6xl:hidden"
              role="group"
              aria-label="Categorías"
            >
              @for (c of categoriasVisibles(); track c.id) {
                <button
                  type="button"
                  class="shrink-0 rounded-full border px-3 py-1 text-xs font-medium uppercase transition"
                  [class]="
                    categoria() === c.id
                      ? 'border-brand-500 bg-brand-600 text-white'
                      : 'border-slate-300 bg-white text-slate-600 hover:bg-slate-50'
                  "
                  [attr.aria-pressed]="categoria() === c.id"
                  (click)="categoria.set(c.id)"
                >
                  {{ c.nombre }}
                </button>
              }
            </div>
            <div class="flex flex-wrap gap-2">
              <label class="relative w-40 shrink-0">
                <svg
                  class="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-slate-400"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path
                    d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3"
                  />
                </svg>
                <input
                  class="input pl-9"
                  placeholder="Escanee el código"
                  aria-label="Código de barras"
                  [ngModel]="codigo()"
                  (ngModelChange)="codigo.set($event)"
                  (keydown.enter)="escanear()"
                />
              </label>
              <div class="flex min-w-[16rem] flex-1">
                <input
                  class="input min-w-0 rounded-r-none uppercase placeholder:normal-case"
                  placeholder="Buscar…"
                  aria-label="Buscar servicio o producto"
                  [ngModel]="filtro()"
                  (ngModelChange)="filtro.set($event)"
                />
                <select
                  class="border-y border-orange-500 bg-orange-400 px-2 text-sm font-medium text-white outline-none"
                  aria-label="Tipo"
                  [ngModel]="tipo()"
                  (ngModelChange)="tipo.set($event)"
                >
                  <option value="TODOS">Todos</option>
                  <option value="SERVICIO">Servicios</option>
                  <option value="PRODUCTO">Productos</option>
                </select>
                <select
                  class="rounded-r-lg bg-brand-600 px-2 text-sm font-medium text-white outline-none"
                  aria-label="Ordenar"
                  [ngModel]="orden()"
                  (ngModelChange)="orden.set($event)"
                >
                  <option value="nombre">A-Z</option>
                  <option value="precio">Precio ↑</option>
                  <option value="precioDesc">Precio ↓</option>
                </select>
              </div>
            </div>

            <div class="min-h-0 flex-1 overflow-auto pr-1 @max-4xl:max-h-[28rem]">
              <div class="grid grid-cols-[repeat(auto-fill,minmax(9.5rem,1fr))] gap-3">
                @for (it of itemsVisibles(); track it.tipo + it.id) {
                  <div
                    class="relative flex flex-col rounded-xl border-2 bg-white p-2 shadow-sm transition"
                    [class]="
                      enCarrito().has(it.tipo + it.id)
                        ? 'border-brand-300 bg-brand-50'
                        : 'border-transparent hover:border-slate-200'
                    "
                    [class.opacity-50]="it.agotado"
                  >
                    <button
                      type="button"
                      class="flex flex-1 flex-col text-left disabled:cursor-not-allowed"
                      [disabled]="it.agotado"
                      (click)="agregar(it)"
                    >
                      <div
                        class="flex h-28 w-full items-center justify-center rounded-lg border-2 border-dashed border-slate-300 bg-slate-50 text-3xl font-semibold text-slate-300"
                        aria-hidden="true"
                      >
                        {{ iniciales(it.nombre) }}
                      </div>
                      <span
                        class="absolute top-3 right-3 rounded px-2 py-0.5 text-[11px] font-medium text-white"
                        [class]="it.tipo === 'SERVICIO' ? 'bg-orange-400' : 'bg-sky-500'"
                      >
                        {{ it.tipo === 'SERVICIO' ? 'Servicio' : 'Producto' }}
                      </span>
                      <span
                        class="mt-2 line-clamp-2 text-xs font-medium text-slate-700 uppercase"
                        >{{ it.nombre }}</span
                      >
                      <span class="text-xs text-slate-500">{{ it.codigo }}</span>
                      <span class="mt-auto pt-1 pr-9 text-sm font-bold text-slate-900">{{
                        it.precio | money
                      }}</span>
                    </button>
                    <button
                      type="button"
                      class="absolute right-2 bottom-2 flex size-8 items-center justify-center rounded-full border-2 border-brand-200 text-brand-600 hover:bg-brand-100"
                      aria-label="Ver detalle"
                      (click)="verDetalle(it)"
                    >
                      <svg
                        class="size-4"
                        viewBox="0 0 24 24"
                        fill="none"
                        stroke="currentColor"
                        stroke-width="2"
                      >
                        <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" />
                        <circle cx="12" cy="12" r="3" />
                      </svg>
                    </button>
                  </div>
                } @empty {
                  <p class="col-span-full p-6 text-center text-sm text-slate-500">
                    No hay ítems que coincidan.
                  </p>
                }
              </div>
            </div>
          </section>

          <!-- Venta -->
          <aside class="card flex min-h-0 flex-col">
            <div class="grid grid-cols-[1fr_auto_minmax(0,12rem)] gap-2 p-3">
              <!-- Cliente -->
              <div class="relative">
                @if (cliente(); as c) {
                  <div class="input flex items-center justify-between gap-2 uppercase">
                    <span class="truncate">{{ c.ciNit ? c.ciNit + ' - ' : '' }}{{ c.nombre }}</span>
                    <button
                      type="button"
                      class="text-slate-400 hover:text-slate-700"
                      (click)="cliente.set(null)"
                      aria-label="Quitar cliente"
                    >
                      ✕
                    </button>
                  </div>
                } @else {
                  <input
                    class="input"
                    placeholder="Cliente: nombre, teléfono o CI/NIT"
                    [ngModel]="qCliente()"
                    (ngModelChange)="buscarCliente($event)"
                  />
                  @if (clientesEncontrados().length) {
                    <ul
                      class="absolute z-10 mt-1 max-h-56 w-full overflow-auto rounded-lg border border-slate-200 bg-white shadow-lg"
                    >
                      @for (c of clientesEncontrados(); track c.id) {
                        <li>
                          <button
                            type="button"
                            class="w-full px-3 py-2 text-left text-sm hover:bg-slate-100"
                            (click)="elegirCliente(c)"
                          >
                            {{ c.nombre }}
                            <span class="text-slate-400">{{ c.ciNit ?? c.telefono }}</span>
                          </button>
                        </li>
                      }
                    </ul>
                  }
                }
              </div>
              <button
                type="button"
                class="btn-secondary px-3"
                (click)="nuevoCliente()"
                title="Nuevo cliente"
                aria-label="Nuevo cliente"
              >
                <svg
                  class="size-4"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <path d="M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z" />
                </svg>
              </button>
              <select
                class="input"
                aria-label="Estilista por defecto"
                title="Estilista que se asigna a los servicios agregados"
                [ngModel]="estilistaDefecto()"
                (ngModelChange)="cambiarEstilistaDefecto($event)"
              >
                <option [ngValue]="null">— Estilista —</option>
                @for (e of estilistas(); track e.id) {
                  <option [ngValue]="e.id">{{ e.nombre }}</option>
                }
              </select>
              <div class="input col-span-2 flex items-center gap-2 text-slate-600">
                <svg
                  class="size-4 text-slate-400"
                  viewBox="0 0 24 24"
                  fill="none"
                  stroke="currentColor"
                  stroke-width="2"
                >
                  <rect x="3" y="5" width="18" height="16" rx="2" />
                  <path d="M3 10h18M8 3v4M16 3v4" />
                </svg>
                {{ hoy }}
              </div>
            </div>

            <!-- Detalle -->
            <div class="min-h-40 flex-1 overflow-auto border-y border-slate-200">
              <table class="w-full text-sm">
                <thead
                  class="sticky top-0 z-1 bg-slate-100 text-xs font-semibold text-slate-500 uppercase"
                >
                  <tr>
                    <th class="px-2 py-2 text-left">Detalle</th>
                    <th class="w-14 px-1 py-2 text-left">Cant</th>
                    <th class="w-18 px-1 py-2 text-left">P.U.</th>
                    <th class="w-18 px-1 py-2 text-left">Desc</th>
                    <th class="w-20 px-1 py-2 text-right">Total</th>
                    <th class="w-16"><span class="sr-only">Acciones</span></th>
                  </tr>
                </thead>
                <tbody>
                  @for (l of carrito(); track l.key) {
                    <tr class="border-b border-slate-100 align-middle">
                      <td class="px-2 py-1.5">
                        <p class="text-xs font-medium text-slate-700 uppercase">{{ l.nombre }}</p>
                        @if (l.tipo === 'SERVICIO') {
                          @if (editando() === l.key) {
                            <select
                              class="input mt-1 py-0.5 text-xs"
                              aria-label="Estilista"
                              [ngModel]="l.estilistaId"
                              (ngModelChange)="
                                actualizar(l, { estilistaId: $event }); editando.set(null)
                              "
                            >
                              <option [ngValue]="null">— Estilista —</option>
                              @for (e of estilistas(); track e.id) {
                                <option [ngValue]="e.id">{{ e.nombre }}</option>
                              }
                            </select>
                          } @else {
                            <p
                              class="text-[11px]"
                              [class]="l.estilistaId ? 'text-slate-400' : 'text-amber-600'"
                            >
                              {{ nombreEstilista(l.estilistaId) }}
                            </p>
                          }
                        }
                      </td>
                      <td class="px-1">
                        <input
                          class="input px-1.5 py-1 text-right"
                          type="number"
                          min="1"
                          step="1"
                          aria-label="Cantidad"
                          [ngModel]="l.cantidad"
                          (ngModelChange)="cambiarCantidad(l, +$event)"
                        />
                      </td>
                      <td class="px-1">
                        <input
                          class="input px-1.5 py-1 text-right"
                          type="number"
                          min="0"
                          step="0.5"
                          aria-label="Precio unitario"
                          [ngModel]="l.precio"
                          (ngModelChange)="cambiarPrecio(l, +$event)"
                        />
                      </td>
                      <td class="px-1">
                        <input
                          class="input px-1.5 py-1 text-right"
                          type="number"
                          min="0"
                          step="0.5"
                          aria-label="Descuento de la línea"
                          [ngModel]="l.descuento"
                          (ngModelChange)="cambiarDescuento(l, +$event)"
                        />
                      </td>
                      <td class="px-1 text-right font-medium whitespace-nowrap">
                        {{ totalLinea(l) | money }}
                      </td>
                      <td class="px-1">
                        <div class="flex items-center justify-end gap-1.5">
                          <button
                            type="button"
                            class="text-red-500 hover:text-red-700"
                            title="Quitar"
                            aria-label="Quitar"
                            (click)="quitar(l)"
                          >
                            <svg
                              class="size-4"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              stroke-width="2"
                            >
                              <path
                                d="M4 7h16M10 11v6M14 11v6M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2l1-12M9 7V4h6v3"
                              />
                            </svg>
                          </button>
                          <button
                            type="button"
                            class="text-orange-500 hover:text-orange-700 disabled:invisible"
                            title="Cambiar estilista"
                            aria-label="Cambiar estilista"
                            [disabled]="l.tipo !== 'SERVICIO'"
                            (click)="editando.set(editando() === l.key ? null : l.key)"
                          >
                            <svg
                              class="size-4"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              stroke-width="2"
                            >
                              <path
                                d="M11 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-6M17.5 2.5a2.1 2.1 0 0 1 3 3L12 14l-4 1 1-4Z"
                              />
                            </svg>
                          </button>
                          <button
                            type="button"
                            class="hover:text-brand-700"
                            [class]="esCortesia(l) ? 'text-brand-600' : 'text-slate-500'"
                            title="Cortesía (descuento total de la línea)"
                            aria-label="Cortesía"
                            [attr.aria-pressed]="esCortesia(l)"
                            (click)="cortesia(l)"
                          >
                            <svg
                              class="size-4"
                              viewBox="0 0 24 24"
                              fill="none"
                              stroke="currentColor"
                              stroke-width="2"
                            >
                              <rect x="3" y="8" width="18" height="4" rx="1" />
                              <path
                                d="M12 8v13M19 12v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7M7.5 8a2.5 2.5 0 0 1 0-5C10 3 12 8 12 8s2-5 4.5-5a2.5 2.5 0 0 1 0 5"
                              />
                            </svg>
                          </button>
                        </div>
                      </td>
                    </tr>
                  } @empty {
                    <tr>
                      <td colspan="6" class="p-8 text-center text-sm text-slate-400">
                        Toque un servicio o producto para agregarlo.
                      </td>
                    </tr>
                  }
                </tbody>
              </table>
            </div>

            <div class="p-3">
              <input
                class="input"
                placeholder="Observaciones"
                maxlength="500"
                [ngModel]="observaciones()"
                (ngModelChange)="observaciones.set($event)"
              />
            </div>

            <div
              class="grid grid-cols-4 items-end gap-3 border-t border-slate-200 px-3 pt-3 text-center"
            >
              <div>
                <p class="text-[11px] font-semibold text-slate-500 uppercase">Subtotal</p>
                <p class="mt-1 text-xl text-slate-700">{{ subtotal() | money }}</p>
              </div>
              <label>
                <span class="block text-[11px] font-semibold text-slate-500 uppercase"
                  >Descuento (%)</span
                >
                <input
                  class="input mt-1 py-1 text-center"
                  type="number"
                  min="0"
                  max="100"
                  step="1"
                  [ngModel]="descuentoPct()"
                  (ngModelChange)="cambiarDescuentoPct(+$event)"
                />
              </label>
              <label>
                <span class="block text-[11px] font-semibold text-slate-500 uppercase"
                  >Descuento</span
                >
                <input
                  class="input mt-1 py-1 text-center"
                  type="number"
                  min="0"
                  step="1"
                  [ngModel]="descuento()"
                  (ngModelChange)="descuento.set(round2(+$event || 0))"
                />
              </label>
              <div>
                <p class="text-[11px] font-semibold text-slate-500 uppercase">Total</p>
                <p class="mt-1 text-xl font-semibold text-slate-900">{{ total() | money }}</p>
              </div>
            </div>

            <div class="px-3 pt-1 text-xs">
              @if (descuento() > subtotal()) {
                <p class="error-text">El descuento no puede superar el subtotal</p>
              }
              @if (faltaEstilista()) {
                <p class="text-amber-600">
                  Hay servicios sin estilista asignado (no sumarán comisión).
                </p>
              }
            </div>

            <div class="flex items-center justify-between gap-2 p-3">
              <button
                type="button"
                class="btn-primary"
                [disabled]="!puedeCobrar()"
                (click)="cobrar()"
              >
                Realizar venta (F4)
              </button>
              <button
                type="button"
                class="btn bg-slate-500 text-white hover:bg-slate-600"
                (click)="limpiar()"
              >
                Restablecer
              </button>
            </div>
          </aside>
        </div>
      </div>
    </div>
  `,
})
export class PosPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly dialog = injectDialog();

  protected readonly round2 = round2;
  protected readonly hoy = new Date().toLocaleDateString('es-BO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  protected readonly servicios = signal<Servicio[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly estilistas = signal<UsuarioResumen[]>([]);
  protected readonly caja = signal<Caja | null>(null);
  protected readonly cajaCargada = signal(false);

  // Filtros del catálogo
  protected readonly categoria = signal(GENERAL);
  protected readonly qCategoria = signal('');
  protected readonly filtro = signal('');
  protected readonly codigo = signal('');
  protected readonly tipo = signal<'TODOS' | TipoItem>('TODOS');
  protected readonly orden = signal<'nombre' | 'precio' | 'precioDesc'>('nombre');

  // Venta en curso
  protected readonly carrito = signal<CartLine[]>([]);
  protected readonly descuento = signal(0);
  protected readonly observaciones = signal('');
  protected readonly estilistaDefecto = signal<number | null>(null);
  protected readonly editando = signal<number | null>(null);
  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly qCliente = signal('');
  protected readonly clientesEncontrados = signal<Cliente[]>([]);
  protected readonly guardando = signal(false);

  private seq = 0;
  private readonly busquedaCliente$ = new Subject<string>();

  private readonly items = computed<ItemPos[]>(() => [
    ...this.servicios().map((s) => ({
      tipo: 'SERVICIO' as const,
      id: s.id,
      codigo: `S-${s.id}`,
      nombre: s.nombre,
      precio: s.precio,
      categoria: s.categoria,
      agotado: false,
      ref: s,
    })),
    ...this.productos().map((p) => ({
      tipo: 'PRODUCTO' as const,
      id: p.id,
      codigo: p.sku ?? `P-${p.id}`,
      nombre: p.nombre,
      precio: p.precio,
      categoria: PRODUCTOS,
      agotado: p.stock === 0,
      ref: p,
    })),
  ]);

  protected readonly categoriasVisibles = computed(() => {
    const q = this.qCategoria().trim().toLowerCase();
    const nombres = [...new Set(this.servicios().map((s) => s.categoria))].sort();
    return [
      { id: GENERAL, nombre: 'General' },
      ...nombres.map((n) => ({ id: n, nombre: n })),
      { id: PRODUCTOS, nombre: 'Productos' },
    ].filter((c) => c.id === GENERAL || !q || c.nombre.toLowerCase().includes(q));
  });

  protected readonly itemsVisibles = computed(() => {
    const f = this.filtro().trim().toLowerCase();
    const cat = this.categoria();
    const tipo = this.tipo();
    const lista = this.items().filter(
      (i) =>
        (cat === GENERAL || i.categoria === cat) &&
        (tipo === 'TODOS' || i.tipo === tipo) &&
        (!f || i.nombre.toLowerCase().includes(f) || i.codigo.toLowerCase().includes(f)),
    );
    const orden = this.orden();
    return lista.sort((a, b) =>
      orden === 'nombre'
        ? a.nombre.localeCompare(b.nombre)
        : orden === 'precio'
          ? a.precio - b.precio
          : b.precio - a.precio,
    );
  });

  protected readonly enCarrito = computed(
    () => new Set(this.carrito().map((l) => l.tipo + l.itemId)),
  );

  /** Suma de las líneas ya con su descuento propio. */
  protected readonly subtotal = computed(() =>
    round2(this.carrito().reduce((a, l) => a + this.totalLinea(l), 0)),
  );
  protected readonly total = computed(() =>
    round2(Math.max(0, this.subtotal() - this.descuento())),
  );
  protected readonly descuentoPct = computed(() =>
    this.subtotal() > 0 ? round2((this.descuento() / this.subtotal()) * 100) : 0,
  );
  protected readonly faltaEstilista = computed(() =>
    this.carrito().some((l) => l.tipo === 'SERVICIO' && !l.estilistaId),
  );
  protected readonly puedeCobrar = computed(
    () =>
      !!this.caja() &&
      this.carrito().length > 0 &&
      this.descuento() <= this.subtotal() &&
      !this.guardando(),
  );

  constructor() {
    this.busquedaCliente$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((q) => (q.trim().length >= 2 ? this.api.clientes.buscar(q, 0, 8) : of(null))),
      )
      .subscribe((page) => this.clientesEncontrados.set(page?.content ?? []));
  }

  ngOnInit(): void {
    this.api.catalogo.servicios(true).subscribe((s) => this.servicios.set(s));
    this.cargarProductos();
    this.api.usuarios.estilistas().subscribe((e) => this.estilistas.set(e));
    this.api.caja.actual().subscribe((c) => {
      this.caja.set(c);
      this.cajaCargada.set(true);
    });
  }

  protected iniciales(nombre: string): string {
    return nombre
      .split(/\s+/)
      .filter((p) => p.length > 2)
      .slice(0, 2)
      .map((p) => p[0].toUpperCase())
      .join('');
  }

  protected nombreEstilista(id: number | null): string {
    return this.estilistas().find((e) => e.id === id)?.nombre ?? 'Sin estilista';
  }

  protected totalLinea(l: CartLine): number {
    return round2(l.precio * l.cantidad - l.descuento);
  }

  protected esCortesia(l: CartLine): boolean {
    return l.descuento > 0 && this.totalLinea(l) === 0;
  }

  protected agregar(it: ItemPos): void {
    if (it.agotado) return;
    const existente = this.carrito().find((l) => l.tipo === it.tipo && l.itemId === it.id);
    if (existente) {
      this.cambiarCantidad(existente, existente.cantidad + 1);
      return;
    }
    this.carrito.update((c) => [
      ...c,
      {
        key: ++this.seq,
        tipo: it.tipo,
        itemId: it.id,
        nombre: it.nombre,
        precio: it.precio,
        cantidad: 1,
        descuento: 0,
        estilistaId: it.tipo === 'SERVICIO' ? this.estilistaDefecto() : null,
        stock: it.tipo === 'PRODUCTO' ? (it.ref as Producto).stock : undefined,
      },
    ]);
  }

  protected verDetalle(it: ItemPos): void {
    const data = { tipo: it.tipo, item: it.ref } as ItemDetalleData;
    openDialog<boolean, ItemDetalleData, ItemDetalleDialog>(
      this.dialog,
      ItemDetalleDialog,
      data,
      '24rem',
    ).closed.subscribe((ok) => ok && this.agregar(it));
  }

  /** Enter en el campo de código: busca por código exacto y lo agrega. */
  protected escanear(): void {
    const cod = this.codigo().trim().toLowerCase();
    if (!cod) return;
    const it = this.items().find((i) => i.codigo.toLowerCase() === cod);
    if (it && !it.agotado) {
      this.agregar(it);
    } else {
      this.toast.info(it ? `${it.nombre} está agotado` : `No existe el código ${this.codigo()}`);
    }
    this.codigo.set('');
  }

  protected cambiarCantidad(l: CartLine, cantidad: number): void {
    cantidad = Math.floor(cantidad);
    if (!cantidad || cantidad < 1) return;
    if (l.stock !== undefined && cantidad > l.stock) {
      this.toast.info(`Solo hay ${l.stock} unidades de ${l.nombre}`);
      cantidad = l.stock;
    }
    this.actualizar(l, { cantidad, descuento: Math.min(l.descuento, l.precio * cantidad) });
  }

  protected cambiarPrecio(l: CartLine, precio: number): void {
    precio = round2(Math.max(0, precio || 0));
    this.actualizar(l, { precio, descuento: Math.min(l.descuento, precio * l.cantidad) });
  }

  protected cambiarDescuento(l: CartLine, descuento: number): void {
    descuento = round2(Math.min(Math.max(0, descuento || 0), l.precio * l.cantidad));
    this.actualizar(l, { descuento });
  }

  protected cortesia(l: CartLine): void {
    this.actualizar(l, { descuento: this.esCortesia(l) ? 0 : round2(l.precio * l.cantidad) });
  }

  protected cambiarDescuentoPct(pct: number): void {
    pct = Math.min(Math.max(0, pct || 0), 100);
    this.descuento.set(round2((this.subtotal() * pct) / 100));
  }

  /** Aplica el estilista elegido a los servicios que aún no tienen uno. */
  protected cambiarEstilistaDefecto(id: number | null): void {
    this.estilistaDefecto.set(id);
    if (id === null) return;
    this.carrito.update((c) =>
      c.map((l) => (l.tipo === 'SERVICIO' && !l.estilistaId ? { ...l, estilistaId: id } : l)),
    );
  }

  protected actualizar(l: CartLine, cambios: Partial<CartLine>): void {
    this.carrito.update((c) => c.map((x) => (x.key === l.key ? { ...x, ...cambios } : x)));
  }

  protected quitar(l: CartLine): void {
    this.carrito.update((c) => c.filter((x) => x.key !== l.key));
  }

  protected limpiar(): void {
    this.carrito.set([]);
    this.descuento.set(0);
    this.observaciones.set('');
    this.cliente.set(null);
    this.editando.set(null);
  }

  protected buscarCliente(q: string): void {
    this.qCliente.set(q);
    this.busquedaCliente$.next(q);
  }

  protected elegirCliente(c: Cliente): void {
    this.cliente.set(c);
    this.qCliente.set('');
    this.clientesEncontrados.set([]);
  }

  protected nuevoCliente(): void {
    openDialog<Cliente, undefined, ClienteFormDialog>(
      this.dialog,
      ClienteFormDialog,
    ).closed.subscribe((c) => {
      if (c) this.elegirCliente(c);
    });
  }

  protected atajoCobrar(e: Event): void {
    e.preventDefault();
    if (this.puedeCobrar() && this.dialog.openDialogs.length === 0) this.cobrar();
  }

  protected cobrar(): void {
    openDialog<CobroResult, CobroData, CobroDialog>(
      this.dialog,
      CobroDialog,
      { total: this.total() },
      '26rem',
    ).closed.subscribe((r) => r && this.registrar(r));
  }

  private registrar(r: CobroResult): void {
    // El backend aplica un único descuento sobre la venta: se envía la suma de los
    // descuentos por línea más el descuento global.
    const descuentoLineas = this.carrito().reduce((a, l) => a + l.descuento, 0);
    const body: VentaRequest = {
      clienteId: this.cliente()?.id ?? null,
      descuento: round2(descuentoLineas + this.descuento()),
      metodoPago: r.metodoPago,
      montoRecibido: r.montoRecibido,
      observaciones: this.observaciones().trim() || null,
      items: this.carrito().map((l) => ({
        tipo: l.tipo,
        itemId: l.itemId,
        cantidad: l.cantidad,
        precioUnitario: l.precio,
        estilistaId: l.estilistaId,
      })),
    };
    this.guardando.set(true);
    this.api.ventas.crear(body).subscribe({
      next: (venta) => {
        this.guardando.set(false);
        this.toast.success(`Venta N° ${venta.id} registrada`);
        this.limpiar();
        this.cargarProductos();
        openDialog(this.dialog, TicketDialog, venta, '24rem');
      },
      error: () => this.guardando.set(false),
    });
  }

  private cargarProductos(): void {
    this.api.catalogo.productos(true).subscribe((p) => this.productos.set(p));
  }
}

function round2(n: number): number {
  return Math.round(n * 100) / 100;
}
