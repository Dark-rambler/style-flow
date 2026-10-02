import { Component, inject, model, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { FormsModule } from '@angular/forms';
import { debounceTime, distinctUntilChanged, of, Subject, switchMap } from 'rxjs';
import { ApiService } from '../../../core/api.service';
import { Cliente } from '../../../core/models';
import { injectDialog, openDialog } from '../../../shared/ui/dialog';
import { ClienteFormDialog } from '../../clientes/cliente-form.dialog';

/**
 * Buscador de cliente con autocompletado y botón de alta rápida.
 * Usa `display: contents`: el buscador y el botón ocupan cada uno su celda en la grilla del padre.
 */
@Component({
  selector: 'sf-cliente-selector',
  imports: [FormsModule],
  host: { class: 'contents' },
  template: `
    <div class="relative">
      @if (cliente(); as c) {
        <div class="input flex items-center justify-between gap-2 uppercase">
          <span class="truncate">{{ c.ciNit ? c.ciNit + ' - ' : '' }}{{ c.nombre }}</span>
          <button
            type="button"
            class="text-slate-400 hover:text-slate-700"
            (click)="cliente.set(null)"
            aria-label="Quitar cliente"
          >
            ✕
          </button>
        </div>
      } @else {
        <input
          class="input"
          placeholder="Cliente: nombre, teléfono o CI/NIT"
          [ngModel]="q()"
          (ngModelChange)="buscar($event)"
        />
        @if (encontrados().length) {
          <ul
            class="absolute z-10 mt-1 max-h-56 w-full overflow-auto rounded-lg border border-slate-200 bg-white shadow-lg"
          >
            @for (c of encontrados(); track c.id) {
              <li>
                <button
                  type="button"
                  class="w-full px-3 py-2 text-left text-sm hover:bg-slate-100"
                  (click)="elegir(c)"
                >
                  {{ c.nombre }}
                  <span class="text-slate-400">{{ c.ciNit ?? c.telefono }}</span>
                </button>
              </li>
            }
          </ul>
        }
      }
    </div>
    <button
      type="button"
      class="btn-secondary px-3"
      (click)="nuevo()"
      title="Nuevo cliente"
      aria-label="Nuevo cliente"
    >
      <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
        <path d="M12 20h9M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4Z" />
      </svg>
    </button>
  `,
})
export class ClienteSelectorComponent {
  private readonly api = inject(ApiService);
  private readonly dialog = injectDialog();

  readonly cliente = model<Cliente | null>(null);

  protected readonly q = signal('');
  protected readonly encontrados = signal<Cliente[]>([]);
  private readonly busqueda$ = new Subject<string>();

  constructor() {
    this.busqueda$
      .pipe(
        debounceTime(250),
        distinctUntilChanged(),
        switchMap((q) => (q.trim().length >= 2 ? this.api.clientes.buscar(q, 0, 8) : of(null))),
        takeUntilDestroyed(),
      )
      .subscribe((page) => this.encontrados.set(page?.content ?? []));
  }

  protected buscar(q: string): void {
    this.q.set(q);
    this.busqueda$.next(q);
  }

  protected elegir(c: Cliente): void {
    this.cliente.set(c);
    this.q.set('');
    this.encontrados.set([]);
  }

  protected nuevo(): void {
    openDialog<Cliente, undefined, ClienteFormDialog>(
      this.dialog,
      ClienteFormDialog,
    ).closed.subscribe((c) => {
      if (c) this.elegir(c);
    });
  }
}
