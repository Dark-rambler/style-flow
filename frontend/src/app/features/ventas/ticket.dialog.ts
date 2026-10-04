import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { DatePipe } from '@angular/common';
import { Component, inject } from '@angular/core';
import { METODOS_PAGO, Venta } from '../../core/models';
import { NegocioStore } from '../../core/negocio.store';
import { MoneyPipe } from '../../shared/money.pipe';

/** Comprobante de venta imprimible (formato ticket 80 mm). */
@Component({
  selector: 'sf-ticket-dialog',
  imports: [MoneyPipe, DatePipe],
  template: `
    <div class="p-6">
      <div class="print-area mx-auto max-w-xs font-mono text-xs text-slate-800">
        <div class="text-center">
          <p class="text-sm font-bold">{{ negocio()?.name }}</p>
          @if (negocio()?.taxId) {
            <p>NIT: {{ negocio()?.taxId }}</p>
          }
          @if (negocio()?.address) {
            <p>{{ negocio()?.address }}</p>
          }
          @if (negocio()?.phone) {
            <p>Tel: {{ negocio()?.phone }}</p>
          }
        </div>
        <hr class="my-2 border-dashed border-slate-400" />
        <p>Venta N° {{ v.id }}</p>
        <p>{{ v.date | date: 'dd/MM/yyyy HH:mm' }}</p>
        <p>Atendió: {{ v.cashier }}</p>
        @if (v.customer) {
          <p>Cliente: {{ v.customer }}{{ v.customerTaxId ? ' (' + v.customerTaxId + ')' : '' }}</p>
        }
        <hr class="my-2 border-dashed border-slate-400" />
        @for (i of v.items; track i.id) {
          <div class="flex justify-between gap-2">
            <span>{{ i.quantity }} x {{ i.description }}</span>
            <span class="whitespace-nowrap">{{ i.unitPrice * i.quantity | money }}</span>
          </div>
          @if (i.discount > 0) {
            <div class="flex justify-between gap-2 pl-4 text-slate-500">
              <span>{{ i.subtotal === 0 ? 'Cortesía' : 'Descuento' }}</span>
              <span class="whitespace-nowrap">-{{ i.discount | money }}</span>
            </div>
          }
          @if (i.stylist) {
            <p class="pl-4 text-slate-500">({{ i.stylist }})</p>
          }
        }
        <hr class="my-2 border-dashed border-slate-400" />
        <div class="flex justify-between">
          <span>Subtotal</span><span>{{ v.subtotal | money }}</span>
        </div>
        @if (v.discount > 0) {
          <div class="flex justify-between">
            <span>Descuento</span><span>-{{ v.discount | money }}</span>
          </div>
        }
        <div class="flex justify-between text-sm font-bold">
          <span>TOTAL</span><span>{{ v.total | money }}</span>
        </div>
        <div class="flex justify-between text-slate-500">
          <span>IVA incluido</span><span>{{ v.tax | money }}</span>
        </div>
        <div class="mt-1 flex justify-between">
          <span>{{ metodo() }}</span
          ><span>{{ v.amountReceived | money }}</span>
        </div>
        @if (v.change > 0) {
          <div class="flex justify-between">
            <span>Cambio</span><span>{{ v.change | money }}</span>
          </div>
        }
        @if (v.notes) {
          <p class="mt-2 text-slate-600">Obs.: {{ v.notes }}</p>
        }
        @if (v.status === 'VOIDED') {
          <p class="mt-2 text-center font-bold text-red-600">*** ANULADA ***</p>
        }
        @if (negocio()?.receiptMessage) {
          <p class="mt-3 text-center">{{ negocio()?.receiptMessage }}</p>
        }
      </div>
      <div class="mt-6 flex justify-end gap-2 print:hidden">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cerrar</button>
        <button type="button" class="btn-primary" (click)="imprimir()">Imprimir</button>
      </div>
    </div>
  `,
})
export class TicketDialog {
  protected readonly v = inject<Venta>(DIALOG_DATA);
  protected readonly ref = inject(DialogRef);
  protected readonly negocio = inject(NegocioStore).negocio;

  protected metodo(): string {
    return (
      METODOS_PAGO.find((m) => m.value === this.v.paymentMethod)?.label ?? this.v.paymentMethod
    );
  }

  protected imprimir(): void {
    window.print();
  }
}
