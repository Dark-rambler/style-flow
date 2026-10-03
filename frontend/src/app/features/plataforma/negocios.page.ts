import { DatePipe } from '@angular/common';
import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { NegocioResumen } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { MoneyPipe } from '../../shared/money.pipe';
import { ConfirmData, ConfirmDialog } from '../../shared/ui/confirm-dialog';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { NegocioFormDialog } from './negocio-form.dialog';

@Component({
  selector: 'sf-negocios-page',
  imports: [FormsModule, DatePipe, MoneyPipe],
  template: `
    <div class="mb-6 flex flex-wrap items-center gap-3">
      <div class="mr-auto">
        <h1 class="page-title">Negocios</h1>
        <p class="text-sm text-slate-500">
          {{ activos() }} activos de {{ negocios().length }} registrados
        </p>
      </div>
      <input
        class="input max-w-xs"
        placeholder="Buscar por nombre o código…"
        [ngModel]="q()"
        (ngModelChange)="q.set($event)"
      />
      <button type="button" class="btn-primary" (click)="nuevo()">+ Nuevo negocio</button>
    </div>

    <div class="card overflow-x-auto">
      <table class="data-table">
        <thead>
          <tr>
            <th>Negocio</th>
            <th>Código de acceso</th>
            <th>Alta</th>
            <th class="text-right">Usuarios</th>
            <th class="text-right">Ventas 30 días</th>
            <th class="text-right">Total 30 días</th>
            <th>Estado</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (n of filtrados(); track n.id) {
            <tr [class.opacity-60]="!n.active">
              <td class="font-medium">{{ n.name }}</td>
              <td>
                <code class="rounded bg-slate-100 px-1.5 py-0.5 text-xs">{{ n.code }}</code>
              </td>
              <td>{{ n.createdAt | date: 'dd/MM/yyyy' }}</td>
              <td class="text-right">{{ n.users }}</td>
              <td class="text-right">{{ n.sales30d }}</td>
              <td class="text-right">{{ n.total30d | money }}</td>
              <td>
                <span
                  class="badge"
                  [class]="n.active ? 'bg-emerald-100 text-emerald-700' : 'bg-red-100 text-red-700'"
                  >{{ n.active ? 'Activo' : 'Suspendido' }}</span
                >
              </td>
              <td class="text-right">
                <button
                  type="button"
                  class="btn-ghost btn-sm"
                  [class.text-red-600]="n.active"
                  (click)="cambiarEstado(n)"
                >
                  {{ n.active ? 'Suspender' : 'Reactivar' }}
                </button>
              </td>
            </tr>
          } @empty {
            <tr>
              <td colspan="8" class="text-center text-slate-400">Sin negocios</td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class NegociosPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  private readonly dialog = injectDialog();

  protected readonly negocios = signal<NegocioResumen[]>([]);
  protected readonly q = signal('');
  protected readonly activos = computed(() => this.negocios().filter((n) => n.active).length);
  protected readonly filtrados = computed(() => {
    const q = this.q().trim().toLowerCase();
    return this.negocios().filter(
      (n) => !q || n.name.toLowerCase().includes(q) || n.code.includes(q),
    );
  });

  ngOnInit(): void {
    this.cargar();
  }

  protected nuevo(): void {
    openDialog<NegocioResumen, undefined, NegocioFormDialog>(
      this.dialog,
      NegocioFormDialog,
      undefined,
      '36rem',
    ).closed.subscribe((n) => n && this.cargar());
  }

  protected cambiarEstado(n: NegocioResumen): void {
    const suspender = n.active;
    const data: ConfirmData = {
      title: suspender ? `Suspender "${n.name}"` : `Reactivar "${n.name}"`,
      message: suspender
        ? 'Sus usuarios no podrán ingresar ni operar hasta que se reactive. Los datos se conservan.'
        : 'Sus usuarios podrán volver a ingresar y operar normalmente.',
      confirmText: suspender ? 'Suspender' : 'Reactivar',
      danger: suspender,
    };
    openDialog(this.dialog, ConfirmDialog, data, '26rem').closed.subscribe((ok) => {
      if (!ok) return;
      this.api.plataforma.cambiarEstado(n.id, !suspender).subscribe(() => {
        this.toast.success(suspender ? 'Negocio suspendido' : 'Negocio reactivado');
        this.cargar();
      });
    });
  }

  private cargar(): void {
    this.api.plataforma.negocios().subscribe((n) => this.negocios.set(n));
  }
}
