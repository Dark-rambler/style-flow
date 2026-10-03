import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { EstilistaTotal, ItemTop, METODOS_PAGO, Resumen, VentaDia } from '../../core/models';
import { NegocioStore } from '../../core/negocio.store';
import { descargar, haceDias, inicioDeMes, isoDate } from '../../shared/dates';
import { MoneyPipe } from '../../shared/money.pipe';
import { VentasChart } from './ventas-chart';

@Component({
  selector: 'sf-reportes-page',
  imports: [FormsModule, MoneyPipe, VentasChart],
  template: `
    <div class="mb-6 flex flex-wrap items-end gap-3">
      <h1 class="page-title mr-auto">Reportes</h1>
      <div class="flex gap-1">
        <button type="button" class="btn-secondary btn-sm" (click)="rango(isoDate(), isoDate())">
          Hoy
        </button>
        <button type="button" class="btn-secondary btn-sm" (click)="rango(haceDias(6), isoDate())">
          7 días
        </button>
        <button
          type="button"
          class="btn-secondary btn-sm"
          (click)="rango(inicioDeMes(), isoDate())"
        >
          Este mes
        </button>
      </div>
      <label class="field"
        ><span class="label">Desde</span
        ><input class="input" type="date" [ngModel]="desde()" (ngModelChange)="desde.set($event)"
      /></label>
      <label class="field"
        ><span class="label">Hasta</span
        ><input class="input" type="date" [ngModel]="hasta()" (ngModelChange)="hasta.set($event)"
      /></label>
      <button type="button" class="btn-primary" (click)="cargar()">Aplicar</button>
      <button type="button" class="btn-secondary" (click)="exportar()">Exportar CSV</button>
    </div>

    @if (resumen(); as r) {
      <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
        <div class="card p-4">
          <p class="text-xs text-slate-500">Total vendido</p>
          <p class="mt-1 text-2xl font-semibold">{{ r.salesTotal | money }}</p>
        </div>
        <div class="card p-4">
          <p class="text-xs text-slate-500">Ventas</p>
          <p class="mt-1 text-2xl font-semibold">{{ r.salesCount }}</p>
          @if (r.voidedSales) {
            <p class="text-xs text-red-600">{{ r.voidedSales }} anuladas</p>
          }
        </div>
        <div class="card p-4">
          <p class="text-xs text-slate-500">Ticket promedio</p>
          <p class="mt-1 text-2xl font-semibold">{{ r.averageTicket | money }}</p>
        </div>
        <div class="card p-4">
          <p class="text-xs text-slate-500">IVA incluido</p>
          <p class="mt-1 text-2xl font-semibold">{{ r.totalTax | money }}</p>
          <p class="text-xs text-slate-500">Descuentos: {{ r.totalDiscounts | money }}</p>
        </div>
      </div>

      <div class="mt-4 grid gap-4 lg:grid-cols-3">
        <div class="card p-5 lg:col-span-2">
          <h2 class="mb-3 text-sm font-semibold">Total vendido por día</h2>
          @if (porDia().length) {
            <sf-ventas-chart [datos]="porDia()" [simbolo]="simbolo()" />
          } @else {
            <p class="py-16 text-center text-sm text-slate-400">Sin ventas en el rango</p>
          }
        </div>
        <div class="card p-5">
          <h2 class="mb-3 text-sm font-semibold">Por método de pago</h2>
          <ul class="flex flex-col gap-3">
            @for (m of r.byPaymentMethod; track m.method) {
              <li>
                <div class="flex justify-between text-sm">
                  <span
                    >{{ metodo(m.method) }}
                    <span class="text-slate-400">({{ m.count }})</span></span
                  ><span class="font-medium">{{ m.total | money }}</span>
                </div>
                <div class="mt-1 h-2 rounded-full bg-slate-100">
                  <div class="h-2 rounded-full bg-brand-600" [style.width.%]="pct(m.total)"></div>
                </div>
              </li>
            } @empty {
              <li class="text-sm text-slate-400">Sin datos</li>
            }
          </ul>
        </div>
      </div>
    }

    <div class="mt-4 grid gap-4 lg:grid-cols-2">
      <div class="card overflow-x-auto">
        <h2 class="px-4 pt-4 pb-2 text-sm font-semibold">Comisiones por estilista</h2>
        <table class="data-table">
          <thead>
            <tr>
              <th>Estilista</th>
              <th class="text-right">Servicios</th>
              <th class="text-right">Producción</th>
              <th class="text-right">%</th>
              <th class="text-right">Comisión</th>
            </tr>
          </thead>
          <tbody>
            @for (e of estilistas(); track e.stylistId) {
              <tr>
                <td class="font-medium">{{ e.stylist }}</td>
                <td class="text-right">{{ e.services }}</td>
                <td class="text-right">{{ e.total | money }}</td>
                <td class="text-right">{{ e.commissionRate }}%</td>
                <td class="text-right font-semibold">{{ e.commission | money }}</td>
              </tr>
            } @empty {
              <tr>
                <td colspan="5" class="text-center text-slate-400">
                  Sin servicios con estilista asignado
                </td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <div class="card overflow-x-auto">
        <div class="flex items-center justify-between px-4 pt-4 pb-2">
          <h2 class="text-sm font-semibold">Más vendidos</h2>
          <select
            class="input w-36 py-1"
            [ngModel]="tipoTop()"
            (ngModelChange)="tipoTop.set($event); cargarTop()"
            aria-label="Tipo"
          >
            <option value="SERVICE">Servicios</option>
            <option value="PRODUCT">Productos</option>
          </select>
        </div>
        <table class="data-table">
          <thead>
            <tr>
              <th>#</th>
              <th>Nombre</th>
              <th class="text-right">Cantidad</th>
              <th class="text-right">Total</th>
            </tr>
          </thead>
          <tbody>
            @for (t of top(); track t.id; let i = $index) {
              <tr>
                <td class="text-slate-400">{{ i + 1 }}</td>
                <td class="font-medium">{{ t.name }}</td>
                <td class="text-right">{{ t.quantity }}</td>
                <td class="text-right">{{ t.total | money }}</td>
              </tr>
            } @empty {
              <tr>
                <td colspan="4" class="text-center text-slate-400">Sin datos</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
    </div>
  `,
})
export class ReportesPage implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly simbolo = inject(NegocioStore).simbolo;
  protected readonly isoDate = isoDate;
  protected readonly haceDias = haceDias;
  protected readonly inicioDeMes = inicioDeMes;

  protected readonly desde = signal(inicioDeMes());
  protected readonly hasta = signal(isoDate());
  protected readonly resumen = signal<Resumen | null>(null);
  protected readonly porDia = signal<VentaDia[]>([]);
  protected readonly estilistas = signal<EstilistaTotal[]>([]);
  protected readonly top = signal<ItemTop[]>([]);
  protected readonly tipoTop = signal<'SERVICE' | 'PRODUCT'>('SERVICE');
  private readonly maxMetodo = computed(() =>
    Math.max(1, ...(this.resumen()?.byPaymentMethod.map((m) => m.total) ?? [])),
  );

  ngOnInit(): void {
    this.cargar();
  }

  protected rango(desde: string, hasta: string): void {
    this.desde.set(desde);
    this.hasta.set(hasta);
    this.cargar();
  }

  protected cargar(): void {
    const r = { from: this.desde(), to: this.hasta() };
    forkJoin({
      resumen: this.api.reportes.resumen(r),
      porDia: this.api.reportes.ventasPorDia(r),
      estilistas: this.api.reportes.estilistas(r),
    }).subscribe((res) => {
      this.resumen.set(res.resumen);
      this.porDia.set(res.porDia);
      this.estilistas.set(res.estilistas);
    });
    this.cargarTop();
  }

  protected cargarTop(): void {
    this.api.reportes
      .top(this.tipoTop(), { from: this.desde(), to: this.hasta() })
      .subscribe((t) => this.top.set(t));
  }

  protected exportar(): void {
    this.api.reportes
      .ventasCsv({ from: this.desde(), to: this.hasta() })
      .subscribe((blob) => descargar(blob, `ventas_${this.desde()}_${this.hasta()}.csv`));
  }

  protected metodo(m: string): string {
    return METODOS_PAGO.find((x) => x.value === m)?.label ?? m;
  }

  protected pct(total: number): number {
    return (total / this.maxMetodo()) * 100;
  }
}
