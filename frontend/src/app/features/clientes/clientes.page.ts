import { CdkTableModule } from '@angular/cdk/table';
import { Component, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { debounceTime, Subject } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { Cliente, Page } from '../../core/models';
import { injectDialog, openDialog } from '../../shared/ui/dialog';
import { Paginator } from '../../shared/ui/paginator';
import { ClienteFormDialog } from './cliente-form.dialog';

@Component({
  selector: 'sf-clientes-page',
  imports: [FormsModule, CdkTableModule, RouterLink, Paginator],
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
          <td cdk-cell *cdkCellDef="let c" class="font-medium">{{ c.name }}</td>
        </ng-container>
        <ng-container cdkColumnDef="telefono">
          <th cdk-header-cell *cdkHeaderCellDef>Teléfono</th>
          <td cdk-cell *cdkCellDef="let c">{{ c.phone ?? '—' }}</td>
        </ng-container>
        <ng-container cdkColumnDef="ciNit">
          <th cdk-header-cell *cdkHeaderCellDef>CI / NIT</th>
          <td cdk-cell *cdkCellDef="let c">{{ c.taxId ?? '—' }}</td>
        </ng-container>
        <ng-container cdkColumnDef="notas">
          <th cdk-header-cell *cdkHeaderCellDef>Notas</th>
          <td cdk-cell *cdkCellDef="let c" class="max-w-xs truncate text-slate-500">
            {{ c.notes ?? '' }}
          </td>
        </ng-container>
        <ng-container cdkColumnDef="acciones">
          <th cdk-header-cell *cdkHeaderCellDef></th>
          <td cdk-cell *cdkCellDef="let c" class="text-right">
            <a
              class="btn-ghost btn-sm"
              routerLink="/ventas"
              [queryParams]="{ clienteId: c.id, cliente: c.name }"
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

    <sf-paginator [page]="page()" (pagina)="cargar($event)" (tamano)="cambiarTamano($event)" />
  `,
})
export class ClientesPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = injectDialog();
  private readonly busqueda$ = new Subject<void>();

  protected readonly columnas = ['nombre', 'telefono', 'ciNit', 'notas', 'acciones'];
  protected readonly q = signal('');
  protected readonly page = signal<Page<Cliente> | null>(null);
  protected readonly tamano = signal(20);

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
    this.api.clientes.buscar(this.q(), page, this.tamano()).subscribe((p) => this.page.set(p));
  }

  protected cambiarTamano(size: number): void {
    this.tamano.set(size);
    this.cargar(0);
  }

  protected editar(c?: Cliente): void {
    openDialog<Cliente, Cliente | undefined, ClienteFormDialog>(
      this.dialog,
      ClienteFormDialog,
      c,
    ).closed.subscribe((r) => r && this.cargar(this.page()?.page ?? 0));
  }
}
