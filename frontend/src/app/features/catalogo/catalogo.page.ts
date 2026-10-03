import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Categoria, Producto, Servicio } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { MoneyPipe } from '../../shared/money.pipe';
import { ConfirmData, ConfirmDialog } from '../../shared/ui/confirm-dialog';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { ProductoFormDialog, ServicioFormData, ServicioFormDialog } from './catalogo-forms';

type Tab = 'servicios' | 'productos' | 'categorias';

@Component({
  selector: 'sf-catalogo-page',
  imports: [FormsModule, MoneyPipe],
  template: `
    <div class="mb-6 flex flex-wrap items-center gap-3">
      <h1 class="page-title mr-auto">Catálogo</h1>
      <div class="inline-flex rounded-lg border border-slate-300 bg-white p-0.5" role="tablist">
        @for (t of tabs; track t.id) {
          <button
            type="button"
            role="tab"
            class="rounded-md px-3 py-1.5 text-sm"
            [attr.aria-selected]="tab() === t.id"
            [class]="tab() === t.id ? 'bg-brand-600 text-white' : 'text-slate-600'"
            (click)="tab.set(t.id)"
          >
            {{ t.label }}
          </button>
        }
      </div>
    </div>

    @switch (tab()) {
      @case ('servicios') {
        <div class="mb-3 flex justify-end">
          <button type="button" class="btn-primary" (click)="editarServicio()">
            + Nuevo servicio
          </button>
        </div>
        <div class="card overflow-x-auto">
          <table class="data-table">
            <thead>
              <tr>
                <th>Servicio</th>
                <th>Categoría</th>
                <th class="text-right">Duración</th>
                <th class="text-right">Precio</th>
                <th>Estado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              @for (s of servicios(); track s.id) {
                <tr [class.opacity-50]="!s.active">
                  <td class="font-medium">
                    <div class="flex items-center gap-3">
                      <img
                        class="size-10 shrink-0 rounded-md border border-slate-200 bg-slate-50"
                        [class]="s.imageUrl ? 'object-cover' : 'object-contain p-1'"
                        [src]="s.imageUrl ?? SIN_IMAGEN"
                        alt=""
                        loading="lazy"
                      />
                      {{ s.name }}
                    </div>
                  </td>
                  <td>{{ s.category }}</td>
                  <td class="text-right">{{ s.durationMinutes }} min</td>
                  <td class="text-right">{{ s.price | money }}</td>
                  <td>
                    <span
                      class="badge"
                      [class]="
                        s.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                      "
                      >{{ s.active ? 'Activo' : 'Inactivo' }}</span
                    >
                  </td>
                  <td class="text-right">
                    <button type="button" class="btn-ghost btn-sm" (click)="editarServicio(s)">
                      Editar
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
      @case ('productos') {
        <div class="mb-3 flex justify-end">
          <button type="button" class="btn-primary" (click)="editarProducto()">
            + Nuevo producto
          </button>
        </div>
        <div class="card overflow-x-auto">
          <table class="data-table">
            <thead>
              <tr>
                <th>Producto</th>
                <th>SKU</th>
                <th class="text-right">Precio</th>
                <th class="text-right">Stock</th>
                <th>Estado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              @for (p of productos(); track p.id) {
                <tr [class.opacity-50]="!p.active">
                  <td class="font-medium">
                    <div class="flex items-center gap-3">
                      <img
                        class="size-10 shrink-0 rounded-md border border-slate-200 bg-slate-50"
                        [class]="p.imageUrl ? 'object-cover' : 'object-contain p-1'"
                        [src]="p.imageUrl ?? SIN_IMAGEN"
                        alt=""
                        loading="lazy"
                      />
                      {{ p.name }}
                    </div>
                  </td>
                  <td class="text-slate-500">{{ p.sku ?? '—' }}</td>
                  <td class="text-right">{{ p.price | money }}</td>
                  <td class="text-right">
                    <span [class]="p.lowStock ? 'font-semibold text-amber-600' : ''">{{
                      p.stock
                    }}</span>
                    <span class="text-xs text-slate-400"> / mín {{ p.minStock }}</span>
                  </td>
                  <td>
                    <span
                      class="badge"
                      [class]="
                        p.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                      "
                      >{{ p.active ? 'Activo' : 'Inactivo' }}</span
                    >
                  </td>
                  <td class="text-right whitespace-nowrap">
                    <button type="button" class="btn-ghost btn-sm" (click)="ajustarStock(p)">
                      Stock ±
                    </button>
                    <button type="button" class="btn-ghost btn-sm" (click)="editarProducto(p)">
                      Editar
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
      @case ('categorias') {
        <form class="mb-3 flex max-w-md gap-2" (ngSubmit)="crearCategoria()">
          <input
            class="input"
            name="nueva"
            placeholder="Nueva categoría"
            [ngModel]="nuevaCategoria()"
            (ngModelChange)="nuevaCategoria.set($event)"
          />
          <button type="submit" class="btn-primary" [disabled]="!nuevaCategoria().trim()">
            Agregar
          </button>
        </form>
        <div class="card max-w-2xl overflow-x-auto">
          <table class="data-table">
            <thead>
              <tr>
                <th>Nombre</th>
                <th>Estado</th>
                <th></th>
              </tr>
            </thead>
            <tbody>
              @for (c of categorias(); track c.id) {
                <tr>
                  <td class="font-medium">{{ c.name }}</td>
                  <td>
                    <button
                      type="button"
                      class="badge"
                      [class]="
                        c.active ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                      "
                      (click)="toggleCategoria(c)"
                      title="Cambiar estado"
                    >
                      {{ c.active ? 'Activa' : 'Inactiva' }}
                    </button>
                  </td>
                  <td class="text-right">
                    <button
                      type="button"
                      class="btn-ghost btn-sm text-red-600"
                      (click)="eliminarCategoria(c)"
                    >
                      Eliminar
                    </button>
                  </td>
                </tr>
              }
            </tbody>
          </table>
        </div>
      }
    }
  `,
})
export class CatalogoPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly dialog = injectDialog();
  protected readonly SIN_IMAGEN = '/image/non-image.png';

  protected readonly tabs: { id: Tab; label: string }[] = [
    { id: 'servicios', label: 'Servicios' },
    { id: 'productos', label: 'Productos' },
    { id: 'categorias', label: 'Categorías' },
  ];
  protected readonly tab = signal<Tab>('servicios');
  protected readonly servicios = signal<Servicio[]>([]);
  protected readonly productos = signal<Producto[]>([]);
  protected readonly categorias = signal<Categoria[]>([]);
  protected readonly nuevaCategoria = signal('');

  ngOnInit(): void {
    this.cargar();
  }

  private cargar(): void {
    this.api.catalogo.servicios().subscribe((s) => this.servicios.set(s));
    this.api.catalogo.productos().subscribe((p) => this.productos.set(p));
    this.api.catalogo.categorias().subscribe((c) => this.categorias.set(c));
  }

  protected editarServicio(servicio?: Servicio): void {
    const data: ServicioFormData = { servicio, categorias: this.categorias() };
    openDialog<Servicio, ServicioFormData, ServicioFormDialog>(
      this.dialog,
      ServicioFormDialog,
      data,
    ).closed.subscribe((r) => r && this.cargar());
  }

  protected editarProducto(p?: Producto): void {
    openDialog<Producto, Producto | undefined, ProductoFormDialog>(
      this.dialog,
      ProductoFormDialog,
      p,
    ).closed.subscribe((r) => r && this.cargar());
  }

  protected ajustarStock(p: Producto): void {
    const data: ConfirmData = {
      title: `Ajustar stock: ${p.name}`,
      message: `Stock actual: ${p.stock}. Ingrese la cantidad a sumar (compra) o restar con signo menos (merma), p. ej. 10 o -2.`,
      inputLabel: 'Cantidad',
      confirmText: 'Aplicar',
    };
    openDialog<string | true, ConfirmData, ConfirmDialog>(
      this.dialog,
      ConfirmDialog,
      data,
      '28rem',
    ).closed.subscribe((v) => {
      const cantidad = Number(v);
      if (typeof v !== 'string' || !Number.isInteger(cantidad) || cantidad === 0) {
        if (typeof v === 'string') this.toast.error('Ingrese un número entero distinto de cero');
        return;
      }
      this.api.catalogo.ajustarStock(p.id, cantidad).subscribe(() => {
        this.toast.success('Stock actualizado');
        this.cargar();
      });
    });
  }

  protected crearCategoria(): void {
    this.api.catalogo.crearCategoria({ name: this.nuevaCategoria().trim() }).subscribe(() => {
      this.nuevaCategoria.set('');
      this.cargar();
    });
  }

  protected toggleCategoria(c: Categoria): void {
    this.api.catalogo
      .actualizarCategoria(c.id, { name: c.name, active: !c.active })
      .subscribe(() => this.cargar());
  }

  protected eliminarCategoria(c: Categoria): void {
    const data: ConfirmData = {
      title: 'Eliminar categoría',
      message: `¿Eliminar "${c.name}"?`,
      confirmText: 'Eliminar',
      danger: true,
    };
    openDialog(this.dialog, ConfirmDialog, data, '24rem').closed.subscribe((ok) => {
      if (ok) this.api.catalogo.eliminarCategoria(c.id).subscribe(() => this.cargar());
    });
  }
}
