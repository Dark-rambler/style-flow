import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, computed, DestroyRef, inject, input, output, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Categoria, Producto, ProductoRequest, Servicio, ServicioRequest } from '../../core/models';
import { ToastService } from '../../core/toast.service';

/** Límite del backend (`spring.servlet.multipart.max-file-size`). */
const MAX_IMAGEN_BYTES = 5 * 1024 * 1024;

/** Foto del ítem: permite elegir una al crear; al editar solo muestra la actual. */
@Component({
  selector: 'sf-foto-picker',
  template: `
    <div class="field col-span-2">
      <span class="label">Foto</span>
      <div class="flex items-center gap-4">
        <img
          class="size-20 shrink-0 rounded-lg border border-slate-200 bg-slate-50"
          [class]="preview() ? 'object-cover' : 'object-contain p-2'"
          [src]="preview() ?? SIN_IMAGEN"
          alt=""
        />
        @if (editable()) {
          <div class="flex flex-col items-start gap-2">
            <label class="btn-secondary btn-sm cursor-pointer">
              {{ preview() ? 'Cambiar foto' : 'Elegir foto' }}
              <input type="file" accept="image/*" class="sr-only" (change)="elegir($event)" />
            </label>
            @if (preview()) {
              <button type="button" class="btn-ghost btn-sm" (click)="quitar()">Quitar</button>
            }
            <span class="text-xs text-slate-400">JPG, PNG o WebP · máx. 5 MB</span>
          </div>
        } @else {
          <span class="text-xs text-slate-400">La foto solo se puede cargar al crear.</span>
        }
      </div>
    </div>
  `,
})
export class FotoPicker {
  readonly actual = input<string | null>(null);
  readonly editable = input(true);
  readonly cambio = output<File | null>();

  private readonly toast = inject(ToastService);
  protected readonly SIN_IMAGEN = '/image/non-image.png';
  private readonly local = signal<string | null>(null);
  protected readonly preview = computed(() => this.local() ?? this.actual());

  constructor() {
    inject(DestroyRef).onDestroy(() => this.revocar());
  }

  protected elegir(event: Event): void {
    const el = event.target as HTMLInputElement;
    const file = el.files?.[0];
    el.value = '';
    if (!file) return;
    if (!file.type.startsWith('image/')) {
      this.toast.error('El archivo debe ser una imagen');
      return;
    }
    if (file.size > MAX_IMAGEN_BYTES) {
      this.toast.error('La imagen supera los 5 MB');
      return;
    }
    this.revocar();
    this.local.set(URL.createObjectURL(file));
    this.cambio.emit(file);
  }

  protected quitar(): void {
    this.revocar();
    this.local.set(null);
    this.cambio.emit(null);
  }

  private revocar(): void {
    const url = this.local();
    if (url) URL.revokeObjectURL(url);
  }
}

export interface ServicioFormData {
  servicio?: Servicio;
  categorias: Categoria[];
}

@Component({
  selector: 'sf-servicio-form',
  imports: [ReactiveFormsModule, FotoPicker],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">
        {{ data.servicio ? 'Editar servicio' : 'Nuevo servicio' }}
      </h2>
      <div class="mt-4 grid grid-cols-2 gap-4">
        <label class="field col-span-2">
          <span class="label">Nombre *</span>
          <input class="input" formControlName="name" />
        </label>
        <label class="field col-span-2">
          <span class="label">Categoría *</span>
          <select class="input" formControlName="categoryId">
            @for (c of data.categorias; track c.id) {
              <option [ngValue]="c.id">{{ c.name }}{{ c.active ? '' : ' (inactiva)' }}</option>
            }
          </select>
        </label>
        <label class="field">
          <span class="label">Precio (Bs) *</span>
          <input class="input" type="number" min="0" step="0.5" formControlName="price" />
        </label>
        <label class="field">
          <span class="label">Duración (min) *</span>
          <input class="input" type="number" min="1" step="5" formControlName="durationMinutes" />
        </label>
        <label class="field col-span-2">
          <span class="label">Descripción</span>
          <textarea class="input" rows="2" formControlName="description"></textarea>
        </label>
        <sf-foto-picker
          class="contents"
          [actual]="s?.imageUrl ?? null"
          [editable]="!s"
          (cambio)="imagen.set($event)"
        />
        <label class="col-span-2 flex items-center gap-2 text-sm">
          <input type="checkbox" formControlName="active" class="size-4 accent-brand-600" /> Activo
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

  protected readonly imagen = signal<File | null>(null);

  protected readonly s = this.data.servicio;
  protected readonly form = inject(NonNullableFormBuilder).group({
    name: [this.s?.name ?? '', [Validators.required, Validators.maxLength(120)]],
    categoryId: [this.s?.categoryId ?? this.data.categorias[0]?.id ?? 0, Validators.required],
    price: [this.s?.price ?? 0, [Validators.required, Validators.min(0)]],
    durationMinutes: [this.s?.durationMinutes ?? 30, [Validators.required, Validators.min(1)]],
    description: [this.s?.description ?? ''],
    active: [this.s?.active ?? true],
  });

  protected guardar(): void {
    this.saving.set(true);
    const body: ServicioRequest = this.form.getRawValue();
    const req = this.s
      ? this.api.catalogo.actualizarServicio(this.s.id, body)
      : this.api.catalogo.crearServicio(body, this.imagen());
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
  imports: [ReactiveFormsModule, FotoPicker],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">{{ p ? 'Editar producto' : 'Nuevo producto' }}</h2>
      <div class="mt-4 grid grid-cols-2 gap-4">
        <label class="field col-span-2">
          <span class="label">Nombre *</span>
          <input class="input" formControlName="name" />
        </label>
        <label class="field">
          <span class="label">SKU / código</span>
          <input class="input" formControlName="sku" />
        </label>
        <label class="field">
          <span class="label">Precio (Bs) *</span>
          <input class="input" type="number" min="0" step="0.5" formControlName="price" />
        </label>
        <label class="field">
          <span class="label">Stock *</span>
          <input class="input" type="number" min="0" formControlName="stock" />
        </label>
        <label class="field">
          <span class="label">Stock mínimo</span>
          <input class="input" type="number" min="0" formControlName="minStock" />
        </label>
        <sf-foto-picker
          class="contents"
          [actual]="p?.imageUrl ?? null"
          [editable]="!p"
          (cambio)="imagen.set($event)"
        />
        <label class="col-span-2 flex items-center gap-2 text-sm">
          <input type="checkbox" formControlName="active" class="size-4 accent-brand-600" /> Activo
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
  protected readonly imagen = signal<File | null>(null);

  protected readonly form = inject(NonNullableFormBuilder).group({
    name: [this.p?.name ?? '', [Validators.required, Validators.maxLength(120)]],
    sku: [this.p?.sku ?? ''],
    price: [this.p?.price ?? 0, [Validators.required, Validators.min(0)]],
    stock: [this.p?.stock ?? 0, [Validators.required, Validators.min(0)]],
    minStock: [this.p?.minStock ?? 0, Validators.min(0)],
    active: [this.p?.active ?? true],
  });

  protected guardar(): void {
    this.saving.set(true);
    const body: ProductoRequest = this.form.getRawValue();
    const req = this.p
      ? this.api.catalogo.actualizarProducto(this.p.id, body)
      : this.api.catalogo.crearProducto(body, this.imagen());
    req.subscribe({
      next: (r) => {
        this.toast.success('Producto guardado');
        this.ref.close(r);
      },
      error: () => this.saving.set(false),
    });
  }
}
