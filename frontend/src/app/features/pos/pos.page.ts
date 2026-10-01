import {
  CdkDrag,
  CdkDragDrop,
  CdkDragHandle,
  CdkDropList,
  moveItemInArray,
} from '@angular/cdk/drag-drop';
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

interface CartLine {
  key: number;
  tipo: TipoItem;
  itemId: number;
  nombre: string;
  precio: number;
  cantidad: number;
  estilistaId: number | null;
  stock?: number;
}

@Component({
  selector: 'sf-pos-page',
  imports: [FormsModule, MoneyPipe, RouterLink, CdkDropList, CdkDrag, CdkDragHandle],
  template: `
    @if (cajaCargada() && !caja()) {
      <div
        class="mb-4 flex items-center justify-between rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-800"
      >
        <span>No hay una caja abierta. Debe abrir caja para registrar ventas.</span>
        <a routerLink="/caja" class="btn-primary btn-sm">Abrir caja</a>
      </div>
    }

    <div class="grid h-full gap-4 xl:grid-cols-[1fr_26rem]">
      <!-- Catálogo -->
      <section class="flex min-h-0 flex-col gap-4">
        <div class="flex flex-wrap items-center gap-3">
          <h1 class="page-title mr-auto">Punto de venta</h1>
          <div class="inline-flex rounded-lg border border-slate-300 bg-white p-0.5" role="tablist">
            <button
              type="button"
              role="tab"
              class="rounded-md px-3 py-1.5 text-sm"
              [attr.aria-selected]="tab() === 'SERVICIO'"
              [class]="tab() === 'SERVICIO' ? 'bg-brand-600 text-white' : 'text-slate-600'"
              (click)="tab.set('SERVICIO')"
            >
              Servicios
            </button>
            <button
              type="button"
              role="tab"
              class="rounded-md px-3 py-1.5 text-sm"
              [attr.aria-selected]="tab() === 'PRODUCTO'"
              [class]="tab() === 'PRODUCTO' ? 'bg-brand-600 text-white' : 'text-slate-600'"
              (click)="tab.set('PRODUCTO')"
            >
              Productos
            </button>
          </div>
          <input
            class="input max-w-xs"
            placeholder="Buscar…"
            [ngModel]="filtro()"
            (ngModelChange)="filtro.set($event)"
          />
        </div>

        @if (tab() === 'SERVICIO') {
          @for (grupo of serviciosPorCategoria(); track grupo.categoria) {
            <div>
              <h2 class="mb-2 text-xs font-semibold tracking-wide text-slate-500 uppercase">
                {{ grupo.categoria }}
              </h2>
              <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 2xl:grid-cols-4">
                @for (s of grupo.servicios; track s.id) {
                  <button
                    type="button"
                    class="card p-4 text-left transition hover:border-brand-300 hover:shadow"
                    (click)="agregarServicio(s)"
                  >
                    <p class="font-medium text-slate-900">{{ s.nombre }}</p>
                    <p class="mt-1 text-xs text-slate-500">{{ s.duracionMin }} min</p>
                    <p class="mt-2 font-semibold text-brand-700">{{ s.precio | money }}</p>
                  </button>
                }
              </div>
            </div>
          } @empty {
            <p class="text-sm text-slate-500">No hay servicios que coincidan.</p>
          }
        } @else {
          <div class="grid grid-cols-2 gap-3 sm:grid-cols-3 2xl:grid-cols-4">
            @for (p of productosFiltrados(); track p.id) {
              <button
                type="button"
                class="card p-4 text-left transition hover:border-brand-300 hover:shadow disabled:opacity-50"
                [disabled]="p.stock === 0"
                (click)="agregarProducto(p)"
              >
                <p class="font-medium text-slate-900">{{ p.nombre }}</p>
                <p class="mt-1 text-xs" [class]="p.stockBajo ? 'text-amber-600' : 'text-slate-500'">
                  Stock: {{ p.stock }}
                </p>
                <p class="mt-2 font-semibold text-brand-700">{{ p.precio | money }}</p>
              </button>
            } @empty {
              <p class="text-sm text-slate-500">No hay productos que coincidan.</p>
            }
          </div>
        }
      </section>

      <!-- Carrito -->
      <aside class="card flex flex-col xl:sticky xl:top-0 xl:max-h-[calc(100vh-8rem)]">
        <div class="border-b border-slate-200 p-4">
          <div class="flex items-center justify-between">
            <h2 class="font-semibold">Venta actual</h2>
            @if (carrito().length) {
              <button type="button" class="btn-ghost btn-sm" (click)="limpiar()">Vaciar</button>
            }
          </div>

          <!-- Cliente -->
          <div class="relative mt-3">
            @if (cliente(); as c) {
              <div
                class="flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2 text-sm"
              >
                <span><span class="text-slate-500">Cliente:</span> {{ c.nombre }}</span>
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
              <div class="flex gap-2">
                <input
                  class="input"
                  placeholder="Cliente (opcional): nombre, teléfono o CI"
                  [ngModel]="qCliente()"
                  (ngModelChange)="buscarCliente($event)"
                />
                <button
                  type="button"
                  class="btn-secondary"
                  (click)="nuevoCliente()"
                  title="Nuevo cliente"
                >
                  +
                </button>
              </div>
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
                        {{ c.nombre }} <span class="text-slate-400">{{ c.telefono }}</span>
                      </button>
                    </li>
                  }
                </ul>
              }
            }
          </div>
        </div>

        <div class="flex-1 overflow-auto p-2" cdkDropList (cdkDropListDropped)="reordenar($event)">
          @for (l of carrito(); track l.key) {
            <div class="group mb-2 rounded-lg border border-slate-200 bg-white p-3" cdkDrag>
              <div class="flex items-start gap-2">
                <span cdkDragHandle class="cursor-grab text-slate-300 select-none" title="Arrastrar"
                  >⋮⋮</span
                >
                <div class="min-w-0 flex-1">
                  <p class="truncate text-sm font-medium">{{ l.nombre }}</p>
                  <div class="mt-2 flex items-center gap-2">
                    <button
                      type="button"
                      class="btn-secondary btn-sm"
                      (click)="cambiarCantidad(l, -1)"
                      aria-label="Menos"
                    >
                      −
                    </button>
                    <span class="w-6 text-center text-sm">{{ l.cantidad }}</span>
                    <button
                      type="button"
                      class="btn-secondary btn-sm"
                      (click)="cambiarCantidad(l, 1)"
                      aria-label="Más"
                    >
                      +
                    </button>
                    <span class="text-xs text-slate-400">×</span>
                    <input
                      class="input w-24 py-1 text-right"
                      type="number"
                      min="0"
                      step="0.5"
                      [ngModel]="l.precio"
                      (ngModelChange)="actualizar(l, { precio: +$event })"
                      aria-label="Precio unitario"
                    />
                  </div>
                  @if (l.tipo === 'SERVICIO') {
                    <select
                      class="input mt-2 py-1"
                      [ngModel]="l.estilistaId"
                      (ngModelChange)="actualizar(l, { estilistaId: $event })"
                      aria-label="Estilista"
                    >
                      <option [ngValue]="null">— Estilista —</option>
                      @for (e of estilistas(); track e.id) {
                        <option [ngValue]="e.id">{{ e.nombre }}</option>
                      }
                    </select>
                  }
                </div>
                <div class="flex flex-col items-end gap-2">
                  <span class="text-sm font-semibold">{{ l.precio * l.cantidad | money }}</span>
                  <button
                    type="button"
                    class="text-xs text-red-500 hover:underline"
                    (click)="quitar(l)"
                  >
                    Quitar
                  </button>
                </div>
              </div>
            </div>
          } @empty {
            <p class="p-6 text-center text-sm text-slate-400">
              Toque un servicio o producto para agregarlo.
            </p>
          }
        </div>

        <div class="space-y-2 border-t border-slate-200 p-4 text-sm">
          <div class="flex justify-between">
            <span class="text-slate-500">Subtotal</span><span>{{ subtotal() | money }}</span>
          </div>
          <div class="flex items-center justify-between gap-2">
            <span class="text-slate-500">Descuento</span>
            <input
              class="input w-28 py-1 text-right"
              type="number"
              min="0"
              step="1"
              [ngModel]="descuento()"
              (ngModelChange)="descuento.set(+$event || 0)"
              aria-label="Descuento"
            />
          </div>
          <div class="flex justify-between pt-1 text-xl font-bold text-slate-900">
            <span>Total</span><span>{{ total() | money }}</span>
          </div>
          @if (descuento() > subtotal()) {
            <p class="error-text">El descuento no puede superar el subtotal</p>
          }
          @if (faltaEstilista()) {
            <p class="text-xs text-amber-600">
              Hay servicios sin estilista asignado (no sumarán comisión).
            </p>
          }
          <button
            type="button"
            class="btn-primary mt-2 w-full py-3 text-base"
            [disabled]="!puedeCobrar()"
            (click)="cobrar()"
          >
            Cobrar {{ total() | money }}
          </button>
        </div>
      </aside>
    </div>
  `,
})
export class PosPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly dialog = injectDialog();

  protected readonly tab = signal<TipoItem>('SERVICIO');
  protected readonly filtro = signal('');
  protected readonly servicios = signal<Servicio[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly estilistas = signal<UsuarioResumen[]>([]);
  protected readonly caja = signal<Caja | null>(null);
  protected readonly cajaCargada = signal(false);

  protected readonly carrito = signal<CartLine[]>([]);
  protected readonly descuento = signal(0);
  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly qCliente = signal('');
  protected readonly clientesEncontrados = signal<Cliente[]>([]);
  protected readonly guardando = signal(false);

  private seq = 0;
  private readonly busquedaCliente$ = new Subject<string>();

  protected readonly serviciosPorCategoria = computed(() => {
    const f = this.filtro().trim().toLowerCase();
    const grupos = new Map<string, Servicio[]>();
    for (const s of this.servicios()) {
      if (f && !s.nombre.toLowerCase().includes(f) && !s.categoria.toLowerCase().includes(f))
        continue;
      grupos.set(s.categoria, [...(grupos.get(s.categoria) ?? []), s]);
    }
    return [...grupos].map(([categoria, servicios]) => ({ categoria, servicios }));
  });

  protected readonly productosFiltrados = computed(() => {
    const f = this.filtro().trim().toLowerCase();
    return this.productos().filter(
      (p) => !f || p.nombre.toLowerCase().includes(f) || p.sku?.toLowerCase().includes(f),
    );
  });

  protected readonly subtotal = computed(() =>
    round2(this.carrito().reduce((a, l) => a + l.precio * l.cantidad, 0)),
  );
  protected readonly total = computed(() =>
    round2(Math.max(0, this.subtotal() - this.descuento())),
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

  protected agregarServicio(s: Servicio): void {
    this.carrito.update((c) => [
      ...c,
      {
        key: ++this.seq,
        tipo: 'SERVICIO',
        itemId: s.id,
        nombre: s.nombre,
        precio: s.precio,
        cantidad: 1,
        estilistaId: null,
      },
    ]);
  }

  protected agregarProducto(p: Producto): void {
    const existente = this.carrito().find((l) => l.tipo === 'PRODUCTO' && l.itemId === p.id);
    if (existente) {
      this.cambiarCantidad(existente, 1);
      return;
    }
    this.carrito.update((c) => [
      ...c,
      {
        key: ++this.seq,
        tipo: 'PRODUCTO',
        itemId: p.id,
        nombre: p.nombre,
        precio: p.precio,
        cantidad: 1,
        estilistaId: null,
        stock: p.stock,
      },
    ]);
  }

  protected cambiarCantidad(l: CartLine, delta: number): void {
    const cantidad = l.cantidad + delta;
    if (cantidad < 1) return;
    if (l.stock !== undefined && cantidad > l.stock) {
      this.toast.info(`Solo hay ${l.stock} unidades de ${l.nombre}`);
      return;
    }
    this.actualizar(l, { cantidad });
  }

  protected actualizar(l: CartLine, cambios: Partial<CartLine>): void {
    this.carrito.update((c) => c.map((x) => (x.key === l.key ? { ...x, ...cambios } : x)));
  }

  protected quitar(l: CartLine): void {
    this.carrito.update((c) => c.filter((x) => x.key !== l.key));
  }

  protected reordenar(e: CdkDragDrop<CartLine[]>): void {
    this.carrito.update((c) => {
      const copia = [...c];
      moveItemInArray(copia, e.previousIndex, e.currentIndex);
      return copia;
    });
  }

  protected limpiar(): void {
    this.carrito.set([]);
    this.descuento.set(0);
    this.cliente.set(null);
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

  protected cobrar(): void {
    openDialog<CobroResult, CobroData, CobroDialog>(
      this.dialog,
      CobroDialog,
      { total: this.total() },
      '26rem',
    ).closed.subscribe((r) => r && this.registrar(r));
  }

  private registrar(r: CobroResult): void {
    const body: VentaRequest = {
      clienteId: this.cliente()?.id ?? null,
      descuento: this.descuento(),
      metodoPago: r.metodoPago,
      montoRecibido: r.montoRecibido,
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
