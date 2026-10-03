import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, inject } from '@angular/core';
import { Producto, Servicio } from '../../core/models';
import { MoneyPipe } from '../../shared/money.pipe';

export type ItemDetalleData =
  { tipo: 'SERVICE'; item: Servicio } | { tipo: 'PRODUCT'; item: Producto };

/** Ficha de un servicio o producto del POS; devuelve `true` si se pide agregarlo. */
@Component({
  selector: 'sf-item-detalle-dialog',
  imports: [MoneyPipe],
  template: `
    <div class="p-6">
      <span class="badge bg-orange-100 text-orange-700">{{
        data.tipo === 'SERVICE' ? 'Servicio' : 'Producto'
      }}</span>
      <h2 class="mt-2 text-lg font-semibold uppercase">{{ data.item.name }}</h2>
      <p class="mt-1 text-2xl font-bold text-brand-700">{{ data.item.price | money }}</p>

      <dl class="mt-4 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
        @if (data.tipo === 'SERVICE') {
          <dt class="text-slate-500">Categoría</dt>
          <dd>{{ data.item.category }}</dd>
          <dt class="text-slate-500">Duración</dt>
          <dd>{{ data.item.durationMinutes }} min</dd>
          @if (data.item.description) {
            <dt class="text-slate-500">Descripción</dt>
            <dd>{{ data.item.description }}</dd>
          }
        } @else {
          <dt class="text-slate-500">Código</dt>
          <dd>{{ data.item.sku ?? '—' }}</dd>
          <dt class="text-slate-500">Stock</dt>
          <dd [class.text-amber-600]="data.item.lowStock">{{ data.item.stock }}</dd>
        }
      </dl>

      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cerrar</button>
        <button
          type="button"
          class="btn-primary"
          [disabled]="data.tipo === 'PRODUCT' && data.item.stock === 0"
          (click)="ref.close(true)"
        >
          Agregar
        </button>
      </div>
    </div>
  `,
})
export class ItemDetalleDialog {
  protected readonly data = inject<ItemDetalleData>(DIALOG_DATA);
  protected readonly ref = inject<DialogRef<boolean>>(DialogRef);
}
