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
          <p class="text-sm font-bold">{{ negocio()?.nombre }}</p>
          @if (negocio()?.nit) {
            <p>NIT: {{ negocio()?.nit }}</p>
          }
          @if (negocio()?.direccion) {
            <p>{{ negocio()?.direccion }}</p>
          }
          @if (negocio()?.telefono) {
            <p>Tel: {{ negocio()?.telefono }}</p>
          }
        </div>
        <hr class="my-2 border-dashed border-slate-400" />
        <p>Venta N° {{ v.id }}</p>
        <p>{{ v.fecha | date: 'dd/MM/yyyy HH:mm' }}</p>
        <p>Atendió: {{ v.cajero }}</p>
        @if (v.cliente) {
          <p>Cliente: {{ v.cliente }}{{ v.clienteCiNit ? ' (' + v.clienteCiNit + ')' : '' }}</p>
        }
        <hr class="my-2 border-dashed border-slate-400" />
        @for (i of v.items; track i.id) {
          <div class="flex justify-between gap-2">
            <span>{{ i.cantidad }} x {{ i.descripcion }}</span>
            <span class="whitespace-nowrap">{{ i.precioUnitario * i.cantidad | money }}</span>
          </div>
          @if (i.descuento > 0) {
            <div class="flex justify-between gap-2 pl-4 text-slate-500">
              <span>{{ i.subtotal === 0 ? 'Cortesía' : 'Descuento' }}</span>
              <span class="whitespace-nowrap">-{{ i.descuento | money }}</span>
            </div>
          }
          @if (i.estilista) {
            <p class="pl-4 text-slate-500">({{ i.estilista }})</p>
          }
        }
        <hr class="my-2 border-dashed border-slate-400" />
        <div class="flex justify-between">
          <span>Subtotal</span><span>{{ v.subtotal | money }}</span>
        </div>
        @if (v.descuento > 0) {
          <div class="flex justify-between">
            <span>Descuento</span><span>-{{ v.descuento | money }}</span>
          </div>
        }
        <div class="flex justify-between text-sm font-bold">
          <span>TOTAL</span><span>{{ v.total | money }}</span>
        </div>
        <div class="flex justify-between text-slate-500">
          <span>IVA incluido</span><span>{{ v.iva | money }}</span>
        </div>
        <div class="mt-1 flex justify-between">
          <span>{{ metodo() }}</span
          ><span>{{ v.montoRecibido | money }}</span>
        </div>
        @if (v.cambio > 0) {
          <div class="flex justify-between">
            <span>Cambio</span><span>{{ v.cambio | money }}</span>
          </div>
        }
        @if (v.observaciones) {
          <p class="mt-2 text-slate-600">Obs.: {{ v.observaciones }}</p>
        }
        @if (v.estado === 'ANULADA') {
          <p class="mt-2 text-center font-bold text-red-600">*** ANULADA ***</p>
        }
        @if (negocio()?.mensajeTicket) {
          <p class="mt-3 text-center">{{ negocio()?.mensajeTicket }}</p>
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
    return METODOS_PAGO.find((m) => m.value === this.v.metodoPago)?.label ?? this.v.metodoPago;
  }

  protected imprimir(): void {
    window.print();
  }
}
