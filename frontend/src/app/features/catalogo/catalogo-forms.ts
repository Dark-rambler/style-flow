import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Categoria, Producto, ProductoRequest, Servicio, ServicioRequest } from '../../core/models';
import { ToastService } from '../../core/toast.service';

export interface ServicioFormData {
  servicio?: Servicio;
  categorias: Categoria[];
}

@Component({
  selector: 'sf-servicio-form',
  imports: [ReactiveFormsModule],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">
        {{ data.servicio ? 'Editar servicio' : 'Nuevo servicio' }}
      </h2>
      <div class="mt-4 grid grid-cols-2 gap-4">
        <label class="field col-span-2">
          <span class="label">Nombre *</span>
          <input class="input" formControlName="nombre" />
        </label>
        <label class="field col-span-2">
          <span class="label">Categoría *</span>
          <select class="input" formControlName="categoriaId">
            @for (c of data.categorias; track c.id) {
              <option [ngValue]="c.id">{{ c.nombre }}{{ c.activo ? '' : ' (inactiva)' }}</option>
            }
          </select>
        </label>
        <label class="field">
          <span class="label">Precio (Bs) *</span>
          <input class="input" type="number" min="0" step="0.5" formControlName="precio" />
        </label>
        <label class="field">
          <span class="label">Duración (min) *</span>
          <input class="input" type="number" min="1" step="5" formControlName="duracionMin" />
        </label>
        <label class="field col-span-2">
          <span class="label">Descripción</span>
          <textarea class="input" rows="2" formControlName="descripcion"></textarea>
        </label>
        <label class="col-span-2 flex items-center gap-2 text-sm">
          <input type="checkbox" formControlName="activo" class="size-4 accent-brand-600" /> Activo
          (visible en el POS)
        </label>
      </div>
      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button type="submit" class="btn-primary" [disabled]="form.invalid || saving()">
          Guardar
        </button>
      </div>
    </form>
  `,
})
export class ServicioFormDialog {
  protected readonly data = inject<ServicioFormData>(DIALOG_DATA);
  protected readonly ref = inject<DialogRef<Servicio>>(DialogRef);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  protected readonly saving = signal(false);

  private readonly s = this.data.servicio;
  protected readonly form = inject(NonNullableFormBuilder).group({
    nombre: [this.s?.nombre ?? '', [Validators.required, Validators.maxLength(120)]],
    categoriaId: [this.s?.categoriaId ?? this.data.categorias[0]?.id ?? 0, Validators.required],
    precio: [this.s?.precio ?? 0, [Validators.required, Validators.min(0)]],
    duracionMin: [this.s?.duracionMin ?? 30, [Validators.required, Validators.min(1)]],
    descripcion: [this.s?.descripcion ?? ''],
    activo: [this.s?.activo ?? true],
  });

  protected guardar(): void {
    this.saving.set(true);
    const body = this.form.getRawValue() as ServicioRequest;
    const req = this.s
      ? this.api.catalogo.actualizarServicio(this.s.id, body)
      : this.api.catalogo.crearServicio(body);
    req.subscribe({
      next: (r) => {
        this.toast.success('Servicio guardado');
        this.ref.close(r);
      },
      error: () => this.saving.set(false),
    });
  }
}

@Component({
  selector: 'sf-producto-form',
  imports: [ReactiveFormsModule],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">{{ p ? 'Editar producto' : 'Nuevo producto' }}</h2>
      <div class="mt-4 grid grid-cols-2 gap-4">
        <label class="field col-span-2">
          <span class="label">Nombre *</span>
          <input class="input" formControlName="nombre" />
        </label>
        <label class="field">
          <span class="label">SKU / código</span>
          <input class="input" formControlName="sku" />
        </label>
        <label class="field">
          <span class="label">Precio (Bs) *</span>
          <input class="input" type="number" min="0" step="0.5" formControlName="precio" />
        </label>
        <label class="field">
          <span class="label">Stock *</span>
          <input class="input" type="number" min="0" formControlName="stock" />
        </label>
        <label class="field">
          <span class="label">Stock mínimo</span>
          <input class="input" type="number" min="0" formControlName="stockMinimo" />
        </label>
        <label class="col-span-2 flex items-center gap-2 text-sm">
          <input type="checkbox" formControlName="activo" class="size-4 accent-brand-600" /> Activo
        </label>
      </div>
      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button type="submit" class="btn-primary" [disabled]="form.invalid || saving()">
          Guardar
        </button>
      </div>
    </form>
  `,
})
export class ProductoFormDialog {
  protected readonly p = inject<Producto | undefined>(DIALOG_DATA);
  protected readonly ref = inject<DialogRef<Producto>>(DialogRef);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  protected readonly saving = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    nombre: [this.p?.nombre ?? '', [Validators.required, Validators.maxLength(120)]],
    sku: [this.p?.sku ?? ''],
    precio: [this.p?.precio ?? 0, [Validators.required, Validators.min(0)]],
    stock: [this.p?.stock ?? 0, [Validators.required, Validators.min(0)]],
    stockMinimo: [this.p?.stockMinimo ?? 0, Validators.min(0)],
    activo: [this.p?.activo ?? true],
  });

  protected guardar(): void {
    this.saving.set(true);
    const body = this.form.getRawValue() as ProductoRequest;
    const req = this.p
      ? this.api.catalogo.actualizarProducto(this.p.id, body)
      : this.api.catalogo.crearProducto(body);
    req.subscribe({
      next: (r) => {
        this.toast.success('Producto guardado');
        this.ref.close(r);
      },
      error: () => this.saving.set(false),
    });
  }
}
