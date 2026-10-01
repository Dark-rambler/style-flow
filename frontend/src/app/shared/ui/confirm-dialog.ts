import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';

export interface ConfirmData {
  title: string;
  message: string;
  confirmText?: string;
  danger?: boolean;
  /** Si se define, pide un texto obligatorio (p. ej. motivo de anulación) y lo devuelve al cerrar. */
  inputLabel?: string;
}

/** Cierra con `true` (o el texto ingresado) al confirmar; `undefined` al cancelar. */
@Component({
  selector: 'sf-confirm-dialog',
  imports: [FormsModule],
  template: `
    <div class="p-6">
      <h2 class="text-lg font-semibold text-slate-900">{{ data.title }}</h2>
      <p class="mt-2 text-sm text-slate-600">{{ data.message }}</p>
      @if (data.inputLabel) {
        <label class="field mt-4">
          <span class="label">{{ data.inputLabel }}</span>
          <input
            class="input"
            [ngModel]="text()"
            (ngModelChange)="text.set($event)"
            maxlength="250"
          />
        </label>
      }
      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button
          type="button"
          [class]="data.danger ? 'btn-danger' : 'btn-primary'"
          [disabled]="!!data.inputLabel && !text().trim()"
          (click)="confirm()"
        >
          {{ data.confirmText ?? 'Confirmar' }}
        </button>
      </div>
    </div>
  `,
})
export class ConfirmDialog {
  protected readonly data = inject<ConfirmData>(DIALOG_DATA);
  protected readonly ref = inject<DialogRef<string | true>>(DialogRef);
  protected readonly text = signal('');

  protected confirm(): void {
    this.ref.close(this.data.inputLabel ? this.text().trim() : true);
  }
}
