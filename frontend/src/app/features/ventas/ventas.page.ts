import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth/auth.service';
import { METODOS_PAGO, Page, VentaResumen } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { isoDate } from '../../shared/dates';
import { MoneyPipe } from '../../shared/money.pipe';
import { ConfirmData, ConfirmDialog } from '../../shared/ui/confirm-dialog';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { TicketDialog } from './ticket.dialog';

@Component({
  selector: 'sf-ventas-page',
  imports: [FormsModule, MoneyPipe, DatePipe],
  template: `
    <div class="mb-6 flex flex-wrap items-end gap-3">
      <h1 class="page-title mr-auto">Ventas</h1>
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
            <tr [class.opacity-60]="v.estado === 'ANULADA'">
              <td class="font-medium">{{ v.id }}</td>
              <td>{{ v.fecha | date: 'dd/MM/yy HH:mm' }}</td>
              <td>{{ v.cliente ?? '—' }}</td>
              <td>{{ v.cajero }}</td>
              <td>{{ metodo(v.metodoPago) }}</td>
              <td class="text-right font-medium">{{ v.total | money }}</td>
              <td>
                <span
                  class="badge"
                  [class]="
                    v.estado === 'ANULADA'
                      ? 'bg-red-100 text-red-700'
                      : 'bg-emerald-100 text-emerald-700'
                  "
                >
                  {{ v.estado === 'ANULADA' ? 'Anulada' : 'Completada' }}
                </span>
              </td>
              <td class="text-right whitespace-nowrap">
                <button type="button" class="btn-ghost btn-sm" (click)="verTicket(v)">
                  Ticket
                </button>
                @if (esAdmin && v.estado === 'COMPLETADA') {
                  <button type="button" class="btn-ghost btn-sm text-red-600" (click)="anular(v)">
                    Anular
                  </button>
                }
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="8" class="text-center text-slate-400">
                No hay ventas en el rango seleccionado
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>

    @if (page(); as p) {
      @if (p.totalPages > 1) {
        <div class="mt-4 flex items-center justify-end gap-2 text-sm">
          <button
            type="button"
            class="btn-secondary btn-sm"
            [disabled]="p.page === 0"
            (click)="buscar(p.page - 1)"
          >
            Anterior
          </button>
          <span>Página {{ p.page + 1 }} de {{ p.totalPages }}</span>
          <button
            type="button"
            class="btn-secondary btn-sm"
            [disabled]="p.page + 1 >= p.totalPages"
            (click)="buscar(p.page + 1)"
          >
            Siguiente
          </button>
        </div>
      }
    }
  `,
})
export class VentasPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly dialog = injectDialog();
  protected readonly esAdmin = inject(AuthService).hasRole('ADMIN');

  protected readonly desde = signal(isoDate());
  protected readonly hasta = signal(isoDate());
  protected readonly page = signal<Page<VentaResumen> | null>(null);

  ngOnInit(): void {
    this.buscar(0);
  }

  protected buscar(page: number): void {
    this.api.ventas
      .buscar({ desde: this.desde(), hasta: this.hasta(), page, size: 20 })
      .subscribe((p) => this.page.set(p));
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
      });
    });
  }
}
