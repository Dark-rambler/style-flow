import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, computed, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { METODOS_PAGO, MetodoPago } from '../../core/models';
import { MoneyPipe } from '../../shared/money.pipe';

export interface CobroData {
  total: number;
}

export interface CobroResult {
  metodoPago: MetodoPago;
  montoRecibido: number | null;
}

@Component({
  selector: 'sf-cobro-dialog',
  imports: [FormsModule, MoneyPipe],
  template: `
    <div class="p-6">
      <h2 class="text-lg font-semibold">Cobrar</h2>
      <p class="mt-1 text-3xl font-bold text-slate-900">{{ data.total | money }}</p>

      <fieldset class="mt-5">
        <legend class="label mb-2">Método de pago</legend>
        <div class="grid grid-cols-2 gap-2">
          @for (m of metodos; track m.value) {
            <button
              type="button"
              class="rounded-lg border px-3 py-3 text-sm font-medium transition"
              [class]="
                metodo() === m.value
                  ? 'border-brand-500 bg-brand-50 text-brand-700'
                  : 'border-slate-300 hover:bg-slate-50'
              "
              [attr.aria-pressed]="metodo() === m.value"
              (click)="metodo.set(m.value)"
            >
              {{ m.label }}
            </button>
          }
        </div>
      </fieldset>

      @if (metodo() === 'CASH') {
        <label class="field mt-5">
          <span class="label">Monto recibido</span>
          <input
            class="input text-lg"
            type="number"
            min="0"
            step="0.5"
            [ngModel]="recibido()"
            (ngModelChange)="recibido.set($event)"
            (keydown.enter)="confirmar()"
          />
        </label>
        <div class="mt-3 flex flex-wrap gap-2">
          @for (b of billetes(); track b) {
            <button type="button" class="btn-secondary btn-sm" (click)="recibido.set(b)">
              {{ b | money }}
            </button>
          }
        </div>
        <div class="mt-4 flex items-center justify-between rounded-lg bg-slate-50 px-4 py-3">
          <span class="text-sm text-slate-600">Cambio</span>
          <span class="text-xl font-semibold" [class.text-red-600]="cambio() < 0">{{
            cambio() | money
          }}</span>
        </div>
      }

      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button type="button" class="btn-primary" [disabled]="!valido()" (click)="confirmar()">
          Confirmar venta
        </button>
      </div>
    </div>
  `,
})
export class CobroDialog {
  protected readonly data = inject<CobroData>(DIALOG_DATA);
  protected readonly ref = inject<DialogRef<CobroResult>>(DialogRef);
  protected readonly metodos = METODOS_PAGO;

  protected readonly metodo = signal<MetodoPago>('CASH');
  protected readonly recibido = signal<number>(this.data.total);
  protected readonly cambio = computed(() => round2((this.recibido() || 0) - this.data.total));
  protected readonly valido = computed(() => this.metodo() !== 'CASH' || this.cambio() >= 0);

  /** Sugerencias de billetes (Bs 10, 20, 50, 100, 200) que cubren el total. */
  protected readonly billetes = computed(() => {
    const t = this.data.total;
    const set = new Set<number>([t]);
    for (const b of [10, 20, 50, 100, 200]) set.add(Math.ceil(t / b) * b);
    return [...set]
      .filter((v) => v >= t)
      .sort((a, b) => a - b)
      .slice(0, 5);
  });

  protected confirmar(): void {
    if (!this.valido()) return;
    this.ref.close({
      metodoPago: this.metodo(),
      montoRecibido: this.metodo() === 'CASH' ? this.recibido() : null,
    });
  }
}

function round2(n: number): number {
  return Math.round(n * 100) / 100;
}
