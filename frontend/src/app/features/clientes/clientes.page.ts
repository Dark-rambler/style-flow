import { CdkTableModule } from '@angular/cdk/table';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, Subject } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Cliente, Page } from '../../core/models';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { ClienteFormDialog } from './cliente-form.dialog';

@Component({
  selector: 'sf-clientes-page',
  imports: [FormsModule, CdkTableModule, RouterLink],
  template: `
    <div class="mb-6 flex flex-wrap items-center gap-3">
      <h1 class="page-title mr-auto">Clientes</h1>
      <input
        class="input max-w-xs"
        placeholder="Buscar por nombre, teléfono o CI…"
        [ngModel]="q()"
        (ngModelChange)="onBuscar($event)"
      />
      <button type="button" class="btn-primary" (click)="editar()">+ Nuevo cliente</button>
    </div>

    <div class="card overflow-x-auto">
      <table cdk-table [dataSource]="page()?.content ?? []" class="data-table">
        <ng-container cdkColumnDef="nombre">
          <th cdk-header-cell *cdkHeaderCellDef>Nombre</th>
          <td cdk-cell *cdkCellDef="let c" class="font-medium">{{ c.nombre }}</td>
        </ng-container>
        <ng-container cdkColumnDef="telefono">
          <th cdk-header-cell *cdkHeaderCellDef>Teléfono</th>
          <td cdk-cell *cdkCellDef="let c">{{ c.telefono ?? '—' }}</td>
        </ng-container>
        <ng-container cdkColumnDef="ciNit">
          <th cdk-header-cell *cdkHeaderCellDef>CI / NIT</th>
          <td cdk-cell *cdkCellDef="let c">{{ c.ciNit ?? '—' }}</td>
        </ng-container>
        <ng-container cdkColumnDef="notas">
          <th cdk-header-cell *cdkHeaderCellDef>Notas</th>
          <td cdk-cell *cdkCellDef="let c" class="max-w-xs truncate text-slate-500">
            {{ c.notas ?? '' }}
          </td>
        </ng-container>
        <ng-container cdkColumnDef="acciones">
          <th cdk-header-cell *cdkHeaderCellDef></th>
          <td cdk-cell *cdkCellDef="let c" class="text-right">
            <a
              class="btn-ghost btn-sm"
              routerLink="/ventas"
              [queryParams]="{ clienteId: c.id, cliente: c.nombre }"
              >Ver ventas</a
            >
            <button type="button" class="btn-ghost btn-sm" (click)="editar(c)">Editar</button>
          </td>
        </ng-container>
        <tr cdk-header-row *cdkHeaderRowDef="columnas"></tr>
        <tr cdk-row *cdkRowDef="let row; columns: columnas"></tr>
        <tr *cdkNoDataRow>
          <td [attr.colspan]="columnas.length" class="text-center text-slate-400">Sin clientes</td>
        </tr>
      </table>
    </div>

    @if (page(); as p) {
      @if (p.totalPages > 1) {
        <div class="mt-4 flex items-center justify-end gap-2 text-sm">
          <button
            type="button"
            class="btn-secondary btn-sm"
            [disabled]="p.page === 0"
            (click)="cargar(p.page - 1)"
          >
            Anterior
          </button>
          <span>Página {{ p.page + 1 }} de {{ p.totalPages }}</span>
          <button
            type="button"
            class="btn-secondary btn-sm"
            [disabled]="p.page + 1 >= p.totalPages"
            (click)="cargar(p.page + 1)"
          >
            Siguiente
          </button>
        </div>
      }
    }
  `,
})
export class ClientesPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = injectDialog();
  private readonly busqueda$ = new Subject<void>();

  protected readonly columnas = ['nombre', 'telefono', 'ciNit', 'notas', 'acciones'];
  protected readonly q = signal('');
  protected readonly page = signal<Page<Cliente> | null>(null);

  constructor() {
    this.busqueda$.pipe(debounceTime(250)).subscribe(() => this.cargar(0));
  }

  ngOnInit(): void {
    this.cargar(0);
  }

  protected onBuscar(q: string): void {
    this.q.set(q);
    this.busqueda$.next();
  }

  protected cargar(page: number): void {
    this.api.clientes.buscar(this.q(), page, 20).subscribe((p) => this.page.set(p));
  }

  protected editar(c?: Cliente): void {
    openDialog<Cliente, Cliente | undefined, ClienteFormDialog>(
      this.dialog,
      ClienteFormDialog,
      c,
    ).closed.subscribe((r) => r && this.cargar(this.page()?.page ?? 0));
  }
}
