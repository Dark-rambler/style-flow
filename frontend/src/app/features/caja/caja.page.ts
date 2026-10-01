import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Caja, METODOS_PAGO } from '../../core/models';
import { CajaStore } from '../../core/caja.store';
import { ToastService } from '../../core/toast.service';
import { MoneyPipe } from '../../shared/money.pipe';
import { ConfirmData, ConfirmDialog } from '../../shared/ui/confirm-dialog';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { AbrirCajaDialog } from './abrir-caja.dialog';

@Component({
  selector: 'sf-caja-page',
  imports: [FormsModule, MoneyPipe, DatePipe],
  template: `
    <h1 class="page-title mb-6">Caja</h1>

    @if (cargando()) {
      <p class="text-sm text-slate-500">Cargando…</p>
    } @else if (caja(); as c) {
      <div class="grid gap-4 lg:grid-cols-3">
        <div class="card p-5 lg:col-span-2">
          <div class="flex items-center justify-between">
            <div>
              <span class="badge bg-emerald-100 text-emerald-700">Abierta</span>
              <p class="mt-2 text-sm text-slate-500">
                Por {{ c.abiertaPor }} · {{ c.abiertaEn | date: 'dd/MM/yyyy HH:mm' }}
              </p>
            </div>
            <button type="button" class="btn-secondary btn-sm" (click)="cargar()">
              Actualizar
            </button>
          </div>
          <dl class="mt-5 grid grid-cols-2 gap-4 sm:grid-cols-4">
            <div>
              <dt class="text-xs text-slate-500">Fondo inicial</dt>
              <dd class="text-lg font-semibold">{{ c.montoInicial | money }}</dd>
            </div>
            <div>
              <dt class="text-xs text-slate-500">Ventas</dt>
              <dd class="text-lg font-semibold">{{ c.cantidadVentas }}</dd>
            </div>
            <div>
              <dt class="text-xs text-slate-500">Total vendido</dt>
              <dd class="text-lg font-semibold">{{ c.totalVentas | money }}</dd>
            </div>
            <div>
              <dt class="text-xs text-slate-500">Efectivo esperado</dt>
              <dd class="text-lg font-semibold text-brand-700">{{ c.efectivoEsperado | money }}</dd>
            </div>
          </dl>
          <h2 class="mt-6 mb-2 text-sm font-semibold">Por método de pago</h2>
          <table class="data-table">
            <thead>
              <tr>
                <th>Método</th>
                <th class="text-right">Ventas</th>
                <th class="text-right">Total</th>
              </tr>
            </thead>
            <tbody>
              @for (m of c.porMetodo; track m.metodo) {
                <tr>
                  <td>{{ etiqueta(m.metodo) }}</td>
                  <td class="text-right">{{ m.cantidad }}</td>
                  <td class="text-right">{{ m.total | money }}</td>
                </tr>
              } @empty {
                <tr>
                  <td colspan="3" class="text-center text-slate-400">Sin ventas todavía</td>
                </tr>
              }
            </tbody>
          </table>
        </div>

        <form class="card flex flex-col gap-4 p-5" (ngSubmit)="cerrar()">
          <h2 class="font-semibold">Cerrar caja (arqueo)</h2>
          <label class="field">
            <span class="label">Efectivo contado</span>
            <input
              class="input text-lg"
              type="number"
              min="0"
              step="0.5"
              name="contado"
              [ngModel]="contado()"
              (ngModelChange)="contado.set($event)"
              required
            />
          </label>
          @if (contado() !== null) {
            <div
              class="rounded-lg px-4 py-3 text-sm"
              [class]="
                diferencia() === 0 ? 'bg-emerald-50 text-emerald-700' : 'bg-amber-50 text-amber-800'
              "
            >
              Diferencia: <strong>{{ diferencia() | money }}</strong>
              {{
                diferencia() === 0 ? '(cuadra)' : diferencia()! > 0 ? '(sobrante)' : '(faltante)'
              }}
            </div>
          }
          <label class="field">
            <span class="label">Observaciones</span>
            <textarea
              class="input"
              rows="2"
              name="obs"
              [ngModel]="observaciones()"
              (ngModelChange)="observaciones.set($event)"
            ></textarea>
          </label>
          <button type="submit" class="btn-danger" [disabled]="contado() === null || procesando()">
            Cerrar caja
          </button>
        </form>
      </div>
    } @else {
      <div class="card flex max-w-md flex-col items-start gap-3 p-6">
        <span class="badge bg-amber-100 text-amber-700">Cerrada</span>
        <h2 class="font-semibold">No hay una caja abierta</h2>
        <p class="text-sm text-slate-500">
          Abra la caja al iniciar el turno con el efectivo disponible. Solo con la caja abierta se
          puede cobrar.
        </p>
        <button type="button" class="btn-primary" (click)="abrir()">Abrir caja</button>
      </div>
    }

    <h2 class="mt-10 mb-3 text-lg font-semibold">Historial de cierres</h2>
    <div class="card overflow-x-auto">
      <table class="data-table">
        <thead>
          <tr>
            <th>Apertura</th>
            <th>Cierre</th>
            <th>Responsable</th>
            <th class="text-right">Total</th>
            <th class="text-right">Esperado</th>
            <th class="text-right">Contado</th>
            <th class="text-right">Diferencia</th>
          </tr>
        </thead>
        <tbody>
          @for (h of historial(); track h.id) {
            <tr>
              <td>{{ h.abiertaEn | date: 'dd/MM/yy HH:mm' }}</td>
              <td>{{ h.cerradaEn ? (h.cerradaEn | date: 'dd/MM/yy HH:mm') : '—' }}</td>
              <td>{{ h.cerradaPor ?? h.abiertaPor }}</td>
              <td class="text-right">{{ h.totalVentas | money }}</td>
              <td class="text-right">{{ h.efectivoEsperado | money }}</td>
              <td class="text-right">{{ h.efectivoContado | money }}</td>
              <td
                class="text-right"
                [class.text-red-600]="(h.diferencia ?? 0) < 0"
                [class.text-emerald-600]="(h.diferencia ?? 0) > 0"
              >
                {{ h.estado === 'ABIERTA' ? 'Abierta' : (h.diferencia | money) }}
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="7" class="text-center text-slate-400">Sin registros</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class CajaPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly dialog = injectDialog();

  private readonly store = inject(CajaStore);
  protected readonly caja = this.store.caja;
  protected readonly cargando = computed(() => !this.store.cargada());
  protected readonly procesando = signal(false);
  protected readonly historial = signal<Caja[]>([]);
  protected readonly contado = signal<number | null>(null);
  protected readonly observaciones = signal('');
  protected readonly diferencia = computed(() => {
    const c = this.caja();
    const contado = this.contado();
    return c && contado !== null ? Math.round((contado - c.efectivoEsperado) * 100) / 100 : null;
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.store.refrescar();
    this.api.caja.historial(0, 15).subscribe((p) => this.historial.set(p.content));
  }

  protected etiqueta(m: string): string {
    return METODOS_PAGO.find((x) => x.value === m)?.label ?? m;
  }

  protected abrir(): void {
    openDialog<Caja, undefined, AbrirCajaDialog>(
      this.dialog,
      AbrirCajaDialog,
      undefined,
      '26rem',
    ).closed.subscribe((c) => c && this.cargar());
  }

  protected cerrar(): void {
    const data: ConfirmData = {
      title: 'Cerrar caja',
      message: 'Después de cerrar no podrá registrar ni anular ventas de este turno. ¿Continuar?',
      confirmText: 'Cerrar caja',
      danger: true,
    };
    openDialog(this.dialog, ConfirmDialog, data, '26rem').closed.subscribe((ok) => {
      if (!ok) return;
      this.procesando.set(true);
      this.store.cerrar(this.contado() ?? 0, this.observaciones() || undefined).subscribe({
        next: (c) => {
          const d = c.diferencia ?? 0;
          this.toast.success(
            d === 0
              ? 'Caja cerrada: el efectivo cuadra'
              : `Caja cerrada con ${d > 0 ? 'sobrante' : 'faltante'} de ${Math.abs(d).toFixed(2)}`,
          );
          this.procesando.set(false);
          this.contado.set(null);
          this.observaciones.set('');
          this.cargar();
        },
        error: () => this.procesando.set(false),
      });
    });
  }
}
