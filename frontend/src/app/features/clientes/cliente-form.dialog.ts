import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Cliente, ClienteRequest } from '../../core/models';
import { ToastService } from '../../core/toast.service';

/** Crear/editar cliente. Cierra devolviendo el Cliente guardado. */
@Component({
  selector: 'sf-cliente-form-dialog',
  imports: [ReactiveFormsModule],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">{{ data ? 'Editar cliente' : 'Nuevo cliente' }}</h2>
      <div class="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <label class="field sm:col-span-2">
          <span class="label">Nombre *</span>
          <input class="input" formControlName="name" />
        </label>
        <label class="field">
          <span class="label">Teléfono</span>
          <input class="input" formControlName="phone" inputmode="tel" />
        </label>
        <label class="field">
          <span class="label">CI / NIT</span>
          <input class="input" formControlName="taxId" />
        </label>
        <label class="field sm:col-span-2">
          <span class="label">Email</span>
          <input class="input" type="email" formControlName="email" />
        </label>
        <label class="field sm:col-span-2">
          <span class="label">Notas</span>
          <textarea
            class="input"
            rows="3"
            formControlName="notes"
            placeholder="Alergias, preferencias, fórmula de color…"
          ></textarea>
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
export class ClienteFormDialog {
  protected readonly data = inject<Cliente | undefined>(DIALOG_DATA);
  protected readonly ref = inject<DialogRef<Cliente>>(DialogRef);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  protected readonly saving = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    name: [this.data?.name ?? '', [Validators.required, Validators.maxLength(120)]],
    phone: [this.data?.phone ?? ''],
    email: [this.data?.email ?? '', Validators.email],
    taxId: [this.data?.taxId ?? ''],
    notes: [this.data?.notes ?? '', Validators.maxLength(500)],
  });

  protected guardar(): void {
    this.saving.set(true);
    const body: ClienteRequest = this.form.getRawValue();
    const req = this.data
      ? this.api.clientes.actualizar(this.data.id, body)
      : this.api.clientes.crear(body);
    req.subscribe({
      next: (c) => {
        this.toast.success('Cliente guardado');
        this.ref.close(c);
      },
      error: () => this.saving.set(false),
    });
  }
}
