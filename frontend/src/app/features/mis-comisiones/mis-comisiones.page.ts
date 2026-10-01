import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { EstilistaTotal } from '../../core/models';
import { inicioDeMes, isoDate } from '../../shared/dates';
import { MoneyPipe } from '../../shared/money.pipe';

@Component({
  selector: 'sf-mis-comisiones-page',
  imports: [FormsModule, MoneyPipe],
  template: `
    <div class="mb-6 flex flex-wrap items-end gap-3">
      <h1 class="page-title mr-auto">Mis comisiones</h1>
      <label class="field"
        ><span class="label">Desde</span
        ><input class="input" type="date" [ngModel]="desde()" (ngModelChange)="desde.set($event)"
      /></label>
      <label class="field"
        ><span class="label">Hasta</span
        ><input class="input" type="date" [ngModel]="hasta()" (ngModelChange)="hasta.set($event)"
      /></label>
      <button type="button" class="btn-primary" (click)="cargar()">Ver</button>
    </div>
    <div class="grid max-w-3xl grid-cols-1 gap-4 sm:grid-cols-3">
      <div class="card p-5">
        <p class="text-xs text-slate-500">Servicios realizados</p>
        <p class="mt-1 text-3xl font-semibold">{{ dato()?.servicios ?? 0 }}</p>
      </div>
      <div class="card p-5">
        <p class="text-xs text-slate-500">Producción</p>
        <p class="mt-1 text-3xl font-semibold">{{ dato()?.total ?? 0 | money }}</p>
      </div>
      <div class="card p-5">
        <p class="text-xs text-slate-500">Comisión ({{ dato()?.comisionPorcentaje ?? 0 }}%)</p>
        <p class="mt-1 text-3xl font-semibold text-brand-700">
          {{ dato()?.comision ?? 0 | money }}
        </p>
      </div>
    </div>
  `,
})
export class MisComisionesPage implements OnInit {
  private readonly api = inject(ApiService);
  protected readonly desde = signal(inicioDeMes());
  protected readonly hasta = signal(isoDate());
  private readonly datos = signal<EstilistaTotal[]>([]);
  protected readonly dato = computed(() => this.datos()[0] ?? null);

  ngOnInit(): void {
    this.cargar();
  }

  protected cargar(): void {
    this.api.reportes
      .misComisiones({ desde: this.desde(), hasta: this.hasta() })
      .subscribe((d) => this.datos.set(d));
  }
}
