import { DatePipe } from '@angular/common';
import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth/auth.service';
import { CajaStore } from '../../core/caja.store';
import { Producto, Resumen, VentaResumen } from '../../core/models';
import { MoneyPipe } from '../../shared/money.pipe';

@Component({
  selector: 'sf-dashboard-page',
  imports: [MoneyPipe, RouterLink, DatePipe],
  template: `
    <div class="mb-6 flex flex-wrap items-center justify-between gap-3">
      <div>
        <h1 class="page-title">Hola, {{ auth.usuario()?.name }}</h1>
        <p class="text-sm text-slate-500">Resumen de hoy</p>
      </div>
      <a routerLink="/cobrar" class="btn-primary">Cobrar</a>
    </div>

    <div class="grid grid-cols-2 gap-4 lg:grid-cols-4">
      <div class="card p-4">
        <p class="text-xs text-slate-500">Vendido hoy</p>
        <p class="mt-1 text-2xl font-semibold">{{ resumen()?.salesTotal | money }}</p>
      </div>
      <div class="card p-4">
        <p class="text-xs text-slate-500">Ventas hoy</p>
        <p class="mt-1 text-2xl font-semibold">{{ resumen()?.salesCount ?? 0 }}</p>
      </div>
      <div class="card p-4">
        <p class="text-xs text-slate-500">Ticket promedio</p>
        <p class="mt-1 text-2xl font-semibold">{{ resumen()?.averageTicket | money }}</p>
      </div>
      <a routerLink="/caja" class="card p-4 hover:border-brand-300">
        <p class="text-xs text-slate-500">Caja</p>
        @if (caja(); as c) {
          <p class="mt-1 text-2xl font-semibold text-emerald-600">Abierta</p>
          <p class="text-xs text-slate-500">Efectivo esperado: {{ c.expectedCash | money }}</p>
        } @else {
          <p class="mt-1 text-2xl font-semibold text-amber-600">Cerrada</p>
          <p class="text-xs text-slate-500">Abrir para vender →</p>
        }
      </a>
    </div>

    <div class="mt-4 grid gap-4 lg:grid-cols-3">
      <div class="card overflow-x-auto lg:col-span-2">
        <h2 class="px-4 pt-4 pb-2 text-sm font-semibold">Últimas ventas de hoy</h2>
        <table class="data-table">
          <thead>
            <tr>
              <th>N°</th>
              <th>Hora</th>
              <th>Cliente</th>
              <th class="text-right">Total</th>
            </tr>
          </thead>
          <tbody>
            @for (v of ultimas(); track v.id) {
              <tr [class.line-through]="v.status === 'VOIDED'">
                <td>{{ v.id }}</td>
                <td>{{ v.date | date: 'HH:mm' }}</td>
                <td>{{ v.customer ?? '—' }}</td>
                <td class="text-right">{{ v.total | money }}</td>
              </tr>
            } @empty {
              <tr>
                <td colspan="4" class="text-center text-slate-400">Aún no hay ventas hoy</td>
              </tr>
            }
          </tbody>
        </table>
      </div>
      <div class="card p-4">
        <h2 class="mb-3 text-sm font-semibold">Stock bajo</h2>
        <ul class="flex flex-col gap-2 text-sm">
          @for (p of stockBajo(); track p.id) {
            <li class="flex justify-between">
              <span>{{ p.name }}</span
              ><span class="font-semibold text-amber-600">⚠ {{ p.stock }}</span>
            </li>
          } @empty {
            <li class="text-slate-400">Todo en orden</li>
          }
        </ul>
      </div>
    </div>
  `,
})
export class DashboardPage implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly auth = inject(AuthService);
  protected readonly resumen = signal<Resumen | null>(null);
  private readonly cajaStore = inject(CajaStore);
  protected readonly caja = this.cajaStore.caja;
  protected readonly ultimas = signal<VentaResumen[]>([]);
  protected readonly stockBajo = signal<Producto[]>([]);

  ngOnInit(): void {
    this.api.reportes.resumen({}).subscribe((r) => this.resumen.set(r));
    this.cajaStore.refrescar();
    this.api.ventas.buscar({ size: 8 }).subscribe((p) => this.ultimas.set(p.content));
    this.api.catalogo.stockBajo().subscribe((p) => this.stockBajo.set(p));
  }
}
