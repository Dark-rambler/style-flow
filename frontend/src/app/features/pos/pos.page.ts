import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Cliente, Producto, Servicio, UsuarioResumen, VentaRequest } from '../../core/models';
import { CajaStore } from '../../core/caja.store';
import { ToastService } from '../../core/toast.service';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { AbrirCajaDialog } from '../caja/abrir-caja.dialog';
import { TicketDialog } from '../ventas/ticket.dialog';
import { CobroDialog, CobroData, CobroResult } from './cobro.dialog';
import { ItemDetalleData, ItemDetalleDialog } from './item-detalle.dialog';
import { CajaCerradaAvisoComponent } from './components/caja-cerrada-aviso.component';
import { CarritoTablaComponent } from './components/carrito-tabla.component';
import { CatalogoFiltrosComponent } from './components/catalogo-filtros.component';
import { CatalogoGridComponent } from './components/catalogo-grid.component';
import {
  CategoriasChipsComponent,
  CategoriasListaComponent,
} from './components/categorias.component';
import { ClienteSelectorComponent } from './components/cliente-selector.component';
import { VentaResumenComponent } from './components/venta-resumen.component';
import {
  CartLine,
  esCortesia,
  FiltroTipo,
  ItemPos,
  OrdenCatalogo,
  round2,
  totalLinea,
} from './pos.models';

const GENERAL = '__general__';
const PRODUCTOS = '__productos__';

@Component({
  selector: 'sf-pos-page',
  imports: [
    FormsModule,
    CajaCerradaAvisoComponent,
    CarritoTablaComponent,
    CatalogoFiltrosComponent,
    CatalogoGridComponent,
    CategoriasChipsComponent,
    CategoriasListaComponent,
    ClienteSelectorComponent,
    VentaResumenComponent,
  ],
  host: { '(window:keydown.f4)': 'atajoCobrar($event)' },
  template: `
    <!-- El layout responde al ancho disponible (container queries), no al viewport:
         así se adapta tanto con el menú lateral abierto como contraído. -->
    <div class="@container">
      <div class="flex flex-col gap-3 @4xl:h-[calc(100vh-7rem)]">
        @if (cajaCargada() && !caja()) {
          <sf-caja-cerrada-aviso (abrir)="abrirCaja()" />
        }

        <div
          class="grid min-h-0 flex-1 gap-3 @4xl:grid-cols-[minmax(0,1fr)_minmax(26rem,32rem)] @6xl:grid-cols-[12rem_minmax(0,1fr)_minmax(28rem,36rem)]"
        >
          <sf-categorias-lista
            class="hidden @6xl:flex"
            [categorias]="categoriasVisibles()"
            [(seleccionada)]="categoria"
            [(busqueda)]="qCategoria"
          />

          <!-- Catálogo -->
          <section class="flex min-h-0 flex-col gap-3">
            <sf-categorias-chips
              class="@6xl:hidden"
              [categorias]="categoriasVisibles()"
              [(seleccionada)]="categoria"
            />
            <sf-catalogo-filtros
              [(filtro)]="filtro"
              [(tipo)]="tipo"
              [(orden)]="orden"
              (escanear)="escanear($event)"
            />
            <sf-catalogo-grid
              [items]="itemsVisibles()"
              [enCarrito]="enCarrito()"
              (agregar)="agregar($event)"
              (verDetalle)="verDetalle($event)"
            />
          </section>

          <!-- Venta -->
          <aside class="card flex min-h-0 flex-col">
            <div
              class="grid grid-cols-[1fr_auto_minmax(0,12rem)] gap-2 p-3 @max-lg:grid-cols-[minmax(0,1fr)_auto]"
            >
              <sf-cliente-selector [(cliente)]="cliente" />
              <select
                class="input @max-lg:col-span-2"
                aria-label="Estilista por defecto"
                title="Estilista que se asigna a los servicios agregados"
                [ngModel]="estilistaDefecto()"
                (ngModelChange)="cambiarEstilistaDefecto($event)"
              >
                <option [ngValue]="null">— Estilista —</option>
                @for (e of estilistas(); track e.id) {
                  <option [ngValue]="e.id">{{ e.name }}</option>
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

            <sf-carrito-tabla
              [lineas]="carrito()"
              [estilistas]="estilistas()"
              (cambiarCantidad)="cambiarCantidad($event.linea, $event.valor)"
              (cambiarPrecio)="cambiarPrecio($event.linea, $event.valor)"
              (cambiarDescuento)="cambiarDescuento($event.linea, $event.valor)"
              (cambiarEstilista)="actualizar($event.linea, { estilistaId: $event.valor })"
              (cortesia)="cortesia($event)"
              (quitar)="quitar($event)"
            />

            <sf-venta-resumen
              [subtotal]="subtotal()"
              [total]="total()"
              [faltaEstilista]="faltaEstilista()"
              [puedeCobrar]="puedeCobrar()"
              [(descuento)]="descuento"
              [(observaciones)]="observaciones"
              (cobrar)="cobrar()"
              (limpiar)="limpiar()"
            />
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

  protected readonly hoy = new Date().toLocaleDateString('es-BO', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
  });

  protected readonly servicios = signal<Servicio[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly estilistas = signal<UsuarioResumen[]>([]);
  private readonly cajaStore = inject(CajaStore);
  protected readonly caja = this.cajaStore.caja;
  protected readonly cajaCargada = this.cajaStore.cargada;

  // Filtros del catálogo
  protected readonly categoria = signal(GENERAL);
  protected readonly qCategoria = signal('');
  protected readonly filtro = signal('');
  protected readonly tipo = signal<FiltroTipo>('TODOS');
  protected readonly orden = signal<OrdenCatalogo>('nombre');

  // Venta en curso
  protected readonly carrito = signal<CartLine[]>([]);
  protected readonly descuento = signal(0);
  protected readonly observaciones = signal('');
  protected readonly estilistaDefecto = signal<number | null>(null);
  protected readonly cliente = signal<Cliente | null>(null);
  protected readonly guardando = signal(false);

  private seq = 0;

  private readonly items = computed<ItemPos[]>(() => [
    ...this.servicios().map((s) => ({
      tipo: 'SERVICE' as const,
      id: s.id,
      codigo: `S-${s.id}`,
      nombre: s.name,
      precio: s.price,
      categoria: s.category,
      agotado: false,
      imagenUrl: s.imageUrl,
      ref: s,
    })),
    ...this.productos().map((p) => ({
      tipo: 'PRODUCT' as const,
      id: p.id,
      codigo: p.sku ?? `P-${p.id}`,
      nombre: p.name,
      precio: p.price,
      categoria: PRODUCTOS,
      agotado: p.stock === 0,
      stock: p.stock,
      stockBajo: p.lowStock,
      ref: p,
    })),
  ]);

  protected readonly categoriasVisibles = computed(() => {
    const q = this.qCategoria().trim().toLowerCase();
    const nombres = [...new Set(this.servicios().map((s) => s.category))].sort();
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
    round2(this.carrito().reduce((a, l) => a + totalLinea(l), 0)),
  );
  protected readonly total = computed(() =>
    round2(Math.max(0, this.subtotal() - this.descuento())),
  );
  protected readonly faltaEstilista = computed(() =>
    this.carrito().some((l) => l.tipo === 'SERVICE' && !l.estilistaId),
  );
  protected readonly puedeCobrar = computed(
    () =>
      !!this.caja() &&
      this.carrito().length > 0 &&
      this.descuento() <= this.subtotal() &&
      !this.guardando(),
  );

  ngOnInit(): void {
    this.api.catalogo.servicios(true).subscribe((s) => this.servicios.set(s));
    this.cargarProductos();
    this.api.usuarios.estilistas().subscribe((e) => this.estilistas.set(e));
    this.cajaStore.refrescar();
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
        estilistaId: it.tipo === 'SERVICE' ? this.estilistaDefecto() : null,
        stock: it.tipo === 'PRODUCT' ? (it.ref as Producto).stock : undefined,
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

  /** Código leído en el campo de código de barras: busca por código exacto y lo agrega. */
  protected escanear(codigo: string): void {
    const cod = codigo.toLowerCase();
    const it = this.items().find((i) => i.codigo.toLowerCase() === cod);
    if (it && !it.agotado) {
      this.agregar(it);
    } else {
      this.toast.info(it ? `${it.nombre} está agotado` : `No existe el código ${codigo}`);
    }
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
    this.actualizar(l, { descuento: esCortesia(l) ? 0 : round2(l.precio * l.cantidad) });
  }

  /** Aplica el estilista elegido a los servicios que aún no tienen uno. */
  protected cambiarEstilistaDefecto(id: number | null): void {
    this.estilistaDefecto.set(id);
    if (id === null) return;
    this.carrito.update((c) =>
      c.map((l) => (l.tipo === 'SERVICE' && !l.estilistaId ? { ...l, estilistaId: id } : l)),
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
  }

  protected abrirCaja(): void {
    openDialog(this.dialog, AbrirCajaDialog, undefined, '26rem');
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
    // Cada línea viaja con su propio descuento (así las comisiones se calculan sobre lo realmente
    // cobrado por cada servicio) y `discount` es solo el descuento global de la venta.
    const body: VentaRequest = {
      customerId: this.cliente()?.id ?? null,
      discount: this.descuento(),
      paymentMethod: r.metodoPago,
      amountReceived: r.montoRecibido,
      notes: this.observaciones().trim() || null,
      items: this.carrito().map((l) => ({
        type: l.tipo,
        itemId: l.itemId,
        quantity: l.cantidad,
        unitPrice: l.precio,
        discount: l.descuento,
        stylistId: l.estilistaId,
      })),
    };
    this.guardando.set(true);
    this.api.ventas.crear(body).subscribe({
      next: (venta) => {
        this.guardando.set(false);
        this.toast.success(`Venta N° ${venta.id} registrada`);
        this.limpiar();
        this.cargarProductos();
        this.cajaStore.refrescar();
        openDialog(this.dialog, TicketDialog, venta, '24rem');
      },
      error: () => this.guardando.set(false),
    });
  }

  private cargarProductos(): void {
    this.api.catalogo.productos(true).subscribe((p) => this.productos.set(p));
  }
}
