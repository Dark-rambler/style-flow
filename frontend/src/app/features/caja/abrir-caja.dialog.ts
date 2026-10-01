import { DialogRef } from '@angular/cdk/dialog';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CajaStore } from '../../core/caja.store';
import { Caja } from '../../core/models';
import { ToastService } from '../../core/toast.service';

/** Apertura de caja (turno). Se usa desde Caja y desde Cobrar. Cierra con la caja abierta. */
@Component({
  selector: 'sf-abrir-caja-dialog',
  imports: [ReactiveFormsModule],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="abrir()">
      <h2 class="text-lg font-semibold">Abrir caja</h2>
      <p class="mt-1 text-sm text-slate-500">
        Cuente el efectivo con el que inicia el turno. Al cerrar la caja se compara con lo vendido.
      </p>
      <div class="mt-4 flex flex-col gap-4">
        <label class="field">
          <span class="label">Fondo inicial (efectivo)</span>
          <input
            class="input text-lg"
            type="number"
            min="0"
            step="0.5"
            formControlName="montoInicial"
          />
        </label>
        <label class="field">
          <span class="label">Observaciones</span>
          <input class="input" formControlName="observaciones" maxlength="500" />
        </label>
      </div>
      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button type="submit" class="btn-primary" [disabled]="form.invalid || saving()">
          Abrir caja
        </button>
      </div>
    </form>
  `,
})
export class AbrirCajaDialog {
  protected readonly ref = inject<DialogRef<Caja>>(DialogRef);
  private readonly store = inject(CajaStore);
  private readonly toast = inject(ToastService);
  protected readonly saving = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    montoInicial: [0, [Validators.required, Validators.min(0)]],
    observaciones: [''],
  });

  protected abrir(): void {
    const { montoInicial, observaciones } = this.form.getRawValue();
    this.saving.set(true);
    this.store.abrir(montoInicial, observaciones || undefined).subscribe({
      next: (c) => {
        this.toast.success('Caja abierta');
        this.ref.close(c);
      },
      error: () => this.saving.set(false),
    });
  }
}
