import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, inject } from '@angular/core';
import { Producto, Servicio } from '../../core/models';
import { MoneyPipe } from '../../shared/money.pipe';

export type ItemDetalleData =
  { tipo: 'SERVICIO'; item: Servicio } | { tipo: 'PRODUCTO'; item: Producto };

/** Ficha de un servicio o producto del POS; devuelve `true` si se pide agregarlo. */
@Component({
  selector: 'sf-item-detalle-dialog',
  imports: [MoneyPipe],
  template: `
    <div class="p-6">
      <span class="badge bg-orange-100 text-orange-700">{{
        data.tipo === 'SERVICIO' ? 'Servicio' : 'Producto'
      }}</span>
      <h2 class="mt-2 text-lg font-semibold uppercase">{{ data.item.nombre }}</h2>
      <p class="mt-1 text-2xl font-bold text-brand-700">{{ data.item.precio | money }}</p>

      <dl class="mt-4 grid grid-cols-[auto_1fr] gap-x-4 gap-y-1 text-sm">
        @if (data.tipo === 'SERVICIO') {
          <dt class="text-slate-500">Categoría</dt>
          <dd>{{ data.item.categoria }}</dd>
          <dt class="text-slate-500">Duración</dt>
          <dd>{{ data.item.duracionMin }} min</dd>
          @if (data.item.descripcion) {
            <dt class="text-slate-500">Descripción</dt>
            <dd>{{ data.item.descripcion }}</dd>
          }
        } @else {
          <dt class="text-slate-500">Código</dt>
          <dd>{{ data.item.sku ?? '—' }}</dd>
          <dt class="text-slate-500">Stock</dt>
          <dd [class.text-amber-600]="data.item.stockBajo">{{ data.item.stock }}</dd>
        }
      </dl>

      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cerrar</button>
        <button
          type="button"
          class="btn-primary"
          [disabled]="data.tipo === 'PRODUCTO' && data.item.stock === 0"
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
