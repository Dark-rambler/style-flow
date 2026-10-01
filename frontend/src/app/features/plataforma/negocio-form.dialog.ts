import { DialogRef } from '@angular/cdk/dialog';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { NegocioResumen } from '../../core/models';
import { ToastService } from '../../core/toast.service';

/** Alta de un negocio con su administrador. Cierra devolviendo el negocio creado. */
@Component({
  selector: 'sf-negocio-form-dialog',
  imports: [ReactiveFormsModule],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">Nuevo negocio</h2>

      <fieldset class="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <legend class="mb-2 text-xs font-semibold tracking-wide text-slate-500 uppercase">
          Peluquería
        </legend>
        <label class="field sm:col-span-2">
          <span class="label">Nombre *</span>
          <input class="input" formControlName="nombre" (input)="sugerirCodigo()" />
        </label>
        <label class="field sm:col-span-2">
          <span class="label">Código de acceso *</span>
          <input class="input lowercase" formControlName="codigo" spellcheck="false" />
          <span class="text-xs text-slate-500">
            Lo escriben sus usuarios al iniciar sesión. Minúsculas, números y guiones; no se puede
            cambiar.
          </span>
        </label>
        <label class="field">
          <span class="label">NIT</span>
          <input class="input" formControlName="nit" />
        </label>
        <label class="field">
          <span class="label">Teléfono</span>
          <input class="input" formControlName="telefono" />
        </label>
      </fieldset>

      <fieldset class="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <legend class="mb-2 text-xs font-semibold tracking-wide text-slate-500 uppercase">
          Administrador inicial
        </legend>
        <label class="field sm:col-span-2">
          <span class="label">Nombre *</span>
          <input class="input" formControlName="adminNombre" />
        </label>
        <label class="field">
          <span class="label">Usuario *</span>
          <input class="input" formControlName="adminUsername" autocomplete="off" />
        </label>
        <label class="field">
          <span class="label">Contraseña *</span>
          <input
            class="input"
            type="password"
            formControlName="adminPassword"
            autocomplete="new-password"
          />
        </label>
      </fieldset>

      <label class="mt-5 flex items-center gap-2 text-sm">
        <input type="checkbox" formControlName="catalogoBase" class="size-4 accent-brand-600" />
        Cargar catálogo base de servicios (cortes, color, tratamientos, peinados y uñas)
      </label>

      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button type="submit" class="btn-primary" [disabled]="form.invalid || saving()">
          Crear negocio
        </button>
      </div>
    </form>
  `,
})
export class NegocioFormDialog {
  protected readonly ref = inject<DialogRef<NegocioResumen>>(DialogRef);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  protected readonly saving = signal(false);
  private codigoEditado = false;

  protected readonly form = inject(NonNullableFormBuilder).group({
    nombre: ['', [Validators.required, Validators.maxLength(120)]],
    codigo: ['', [Validators.required, Validators.pattern(/^[a-z0-9-]{3,40}$/)]],
    nit: [''],
    telefono: [''],
    adminNombre: ['', Validators.required],
    adminUsername: [
      'admin',
      [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z0-9._-]+$/)],
    ],
    adminPassword: ['', [Validators.required, Validators.minLength(6)]],
    catalogoBase: [true],
  });

  constructor() {
    this.form.controls.codigo.valueChanges.subscribe(() => {
      if (this.form.controls.codigo.dirty) this.codigoEditado = true;
    });
  }

  /** Propone el código a partir del nombre mientras el usuario no lo haya escrito a mano. */
  protected sugerirCodigo(): void {
    if (this.codigoEditado) return;
    const codigo = this.form.controls.nombre.value
      .normalize('NFD')
      .replace(/[̀-ͯ]/g, '')
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, '-')
      .replace(/^-+|-+$/g, '')
      .slice(0, 40);
    this.form.controls.codigo.setValue(codigo);
  }

  protected guardar(): void {
    this.saving.set(true);
    this.api.plataforma.crearNegocio(this.form.getRawValue()).subscribe({
      next: (n) => {
        this.toast.success(`Negocio "${n.nombre}" creado`);
        this.ref.close(n);
      },
      error: () => this.saving.set(false),
    });
  }
}
