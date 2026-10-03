import { Component, computed, input, model, output } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MoneyPipe } from '../../../shared/money.pipe';
import { round2 } from '../pos.models';

/** Observaciones, totales con descuento global, avisos y botones de la venta. */
@Component({
  selector: 'sf-venta-resumen',
  imports: [FormsModule, MoneyPipe],
  host: { class: 'block' },
  template: `
    <div class="p-3">
      <input
        class="input"
        placeholder="Observaciones"
        maxlength="500"
        [(ngModel)]="observaciones"
      />
    </div>

    <div
      class="grid grid-cols-4 items-end gap-3 border-t border-slate-200 px-3 pt-3 text-center @max-lg:grid-cols-2"
    >
      <div>
        <p class="text-[11px] font-semibold text-slate-500 uppercase">Subtotal</p>
        <p class="mt-1 text-xl text-slate-700">{{ subtotal() | money }}</p>
      </div>
      <label>
        <span class="block text-[11px] font-semibold text-slate-500 uppercase">Descuento (%)</span>
        <input
          class="input mt-1 py-1 text-center"
          type="number"
          min="0"
          max="100"
          step="1"
          [ngModel]="descuentoPct()"
          (ngModelChange)="cambiarDescuentoPct(+$event)"
        />
      </label>
      <label>
        <span class="block text-[11px] font-semibold text-slate-500 uppercase">Descuento</span>
        <input
          class="input mt-1 py-1 text-center"
          type="number"
          min="0"
          step="1"
          [ngModel]="descuento()"
          (ngModelChange)="descuento.set(round2(+$event || 0))"
        />
      </label>
      <div>
        <p class="text-[11px] font-semibold text-slate-500 uppercase">Total</p>
        <p class="mt-1 text-xl font-semibold text-slate-900">{{ total() | money }}</p>
      </div>
    </div>

    <div class="px-3 pt-1 text-xs">
      @if (descuento() > subtotal()) {
        <p class="error-text">El descuento no puede superar el subtotal</p>
      }
      @if (faltaEstilista()) {
        <p class="text-amber-600">Hay servicios sin estilista asignado (no sumarán comisión).</p>
      }
    </div>

    <div class="flex items-center justify-between gap-2 p-3">
      <button type="button" class="btn-primary" [disabled]="!puedeCobrar()" (click)="cobrar.emit()">
        Realizar venta (F4)
      </button>
      <button
        type="button"
        class="btn bg-slate-500 text-white hover:bg-slate-600"
        (click)="limpiar.emit()"
      >
        Restablecer
      </button>
    </div>
  `,
})
export class VentaResumenComponent {
  readonly subtotal = input.required<number>();
  readonly total = input.required<number>();
  readonly faltaEstilista = input(false);
  readonly puedeCobrar = input(false);
  /** Descuento global de la venta, en monto. */
  readonly descuento = model(0);
  readonly observaciones = model('');

  readonly cobrar = output<void>();
  readonly limpiar = output<void>();

  protected readonly round2 = round2;
  protected readonly descuentoPct = computed(() =>
    this.subtotal() > 0 ? round2((this.descuento() / this.subtotal()) * 100) : 0,
  );

  protected cambiarDescuentoPct(pct: number): void {
    pct = Math.min(Math.max(0, pct || 0), 100);
    this.descuento.set(round2((this.subtotal() * pct) / 100));
  }
}
