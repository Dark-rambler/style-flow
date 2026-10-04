import { DatePipe } from '@angular/common';
import { Component, effect, inject, input, signal, untracked } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth/auth.service';
import { CajaStore } from '../../core/caja.store';
import { METODOS_PAGO, Page, VentaResumen } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { haceDias, isoDate } from '../../shared/dates';
import { MoneyPipe } from '../../shared/money.pipe';
import { ConfirmData, ConfirmDialog } from '../../shared/ui/confirm-dialog';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { Paginator } from '../../shared/ui/paginator';
import { TicketDialog } from './ticket.dialog';

@Component({
  selector: 'sf-ventas-page',
  imports: [FormsModule, MoneyPipe, DatePipe, Paginator],
  template: `
    <div class="mb-6 flex flex-wrap items-end gap-3">
      <div class="mr-auto">
        <h1 class="page-title">Historial de ventas</h1>
        @if (filtroCliente(); as c) {
          <span class="badge mt-2 gap-1 bg-brand-50 text-brand-700">
            Cliente: {{ c.nombre }}
            <button
              type="button"
              class="ml-1 hover:text-brand-900"
              aria-label="Quitar filtro de cliente"
              (click)="quitarCliente()"
            >
              ✕
            </button>
          </span>
        }
      </div>
      <label class="field"
        ><span class="label">Desde</span
        ><input class="input" type="date" [ngModel]="desde()" (ngModelChange)="desde.set($event)"
      /></label>
      <label class="field"
        ><span class="label">Hasta</span
        ><input class="input" type="date" [ngModel]="hasta()" (ngModelChange)="hasta.set($event)"
      /></label>
      <button type="button" class="btn-primary" (click)="buscar(0)">Buscar</button>
    </div>

    <div class="card overflow-x-auto">
      <table class="data-table">
        <thead>
          <tr>
            <th>N°</th>
            <th>Fecha</th>
            <th>Cliente</th>
            <th>Cajero</th>
            <th>Método</th>
            <th class="text-right">Total</th>
            <th>Estado</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (v of page()?.content; track v.id) {
            <tr [class.opacity-60]="v.status === 'VOIDED'">
              <td class="font-medium">{{ v.id }}</td>
              <td>{{ v.date | date: 'dd/MM/yy HH:mm' }}</td>
              <td>{{ v.customer ?? '—' }}</td>
              <td>{{ v.cashier }}</td>
              <td>{{ metodo(v.paymentMethod) }}</td>
              <td class="text-right font-medium">{{ v.total | money }}</td>
              <td>
                <span
                  class="badge"
                  [class]="
                    v.status === 'VOIDED'
                      ? 'bg-red-100 text-red-700'
                      : 'bg-emerald-100 text-emerald-700'
                  "
                >
                  {{ v.status === 'VOIDED' ? 'Anulada' : 'Completada' }}
                </span>
              </td>
              <td class="text-right whitespace-nowrap">
                <button type="button" class="btn-ghost btn-sm" (click)="verTicket(v)">
                  Ticket
                </button>
                @if (esAdmin && v.status === 'COMPLETED' && v.cashRegisterOpen) {
                  <button type="button" class="btn-ghost btn-sm text-red-600" (click)="anular(v)">
                    Anular
                  </button>
                }
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="8" class="text-center text-slate-400">
                No hay ventas {{ filtroCliente() ? 'de este cliente ' : '' }}en el rango
                seleccionado
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    <sf-paginator [page]="page()" (pagina)="buscar($event)" (tamano)="cambiarTamano($event)" />
  `,
})
export class VentasPage {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly dialog = injectDialog();
  private readonly router = inject(Router);
  private readonly cajaStore = inject(CajaStore);
  protected readonly esAdmin = inject(AuthService).hasRole('ADMIN');

  /** Query params opcionales: `/ventas?clienteId=3&cliente=María` (desde Clientes → Ver ventas). */
  readonly clienteId = input<string>();
  readonly cliente = input<string>();

  protected readonly filtroCliente = signal<{ id: number; nombre: string } | null>(null);
  protected readonly desde = signal(isoDate());
  protected readonly hasta = signal(isoDate());
  protected readonly page = signal<Page<VentaResumen> | null>(null);
  protected readonly tamano = signal(20);

  constructor() {
    // Se reevalúa con cada cambio de URL: entrar desde Clientes filtra, volver a /ventas lo quita.
    effect(() => {
      const id = Number(this.clienteId());
      const nombre = this.cliente();
      untracked(() => {
        if (id) {
          this.filtroCliente.set({ id, nombre: nombre ?? `N° ${id}` });
          this.desde.set(haceDias(365)); // historial del cliente: último año por defecto
        } else if (this.filtroCliente()) {
          this.filtroCliente.set(null);
          this.desde.set(isoDate());
        }
        this.buscar(0);
      });
    });
  }

  protected buscar(page: number): void {
    this.api.ventas
      .buscar({
        from: this.desde(),
        to: this.hasta(),
        customerId: this.filtroCliente()?.id,
        page,
        size: this.tamano(),
      })
      .subscribe((p) => this.page.set(p));
  }

  protected cambiarTamano(size: number): void {
    this.tamano.set(size);
    this.buscar(0);
  }

  protected quitarCliente(): void {
    this.router.navigate(['/ventas'], { replaceUrl: true });
  }

  protected metodo(m: string): string {
    return METODOS_PAGO.find((x) => x.value === m)?.label ?? m;
  }

  protected verTicket(v: VentaResumen): void {
    this.api.ventas
      .obtener(v.id)
      .subscribe((venta) => openDialog(this.dialog, TicketDialog, venta, '24rem'));
  }

  protected anular(v: VentaResumen): void {
    const data: ConfirmData = {
      title: `Anular venta N° ${v.id}`,
      message:
        'Se repondrá el stock de los productos y la venta dejará de sumar en caja y reportes.',
      confirmText: 'Anular venta',
      danger: true,
      inputLabel: 'Motivo',
    };
    openDialog<string | true, ConfirmData, ConfirmDialog>(
      this.dialog,
      ConfirmDialog,
      data,
      '28rem',
    ).closed.subscribe((motivo) => {
      if (typeof motivo !== 'string') return;
      this.api.ventas.anular(v.id, motivo).subscribe(() => {
        this.toast.success('Venta anulada');
        this.buscar(this.page()?.page ?? 0);
        this.cajaStore.refrescar();
      });
    });
  }
}
