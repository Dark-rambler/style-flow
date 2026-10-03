import { Component, computed, input, output } from '@angular/core';
import { Page } from '../../core/models';
import { Icon } from './icon';

/** Elemento de la lista de páginas: número (base 0) o salto `…`. */
type Paso = number | '…';

/**
 * Paginador para los endpoints que devuelven `Page<T>` (`PageResponse` del backend).
 * Uso: `<sf-paginator [page]="page()" (pagina)="cargar($event)" (tamano)="cambiarTamano($event)" />`
 */
@Component({
  selector: 'sf-paginator',
  imports: [Icon],
  template: `
    @if (page(); as p) {
      @if (p.totalElements > 0) {
        <nav
          class="mt-4 flex flex-wrap items-center justify-between gap-3 text-sm text-slate-600"
          aria-label="Paginación"
        >
          <span>
            Mostrando <strong>{{ desde() }}–{{ hasta() }}</strong> de
            <strong>{{ p.totalElements }}</strong>
          </span>

          <div class="flex items-center gap-1">
            <button
              type="button"
              class="btn-ghost btn-sm"
              aria-label="Página anterior"
              [disabled]="p.page === 0"
              (click)="pagina.emit(p.page - 1)"
            >
              <sf-icon name="chevronLeft" class="size-4" />
            </button>
            @for (n of pasos(); track $index) {
              @if (n === '…') {
                <span class="px-1 text-slate-400">…</span>
              } @else {
                <button
                  type="button"
                  class="btn-sm min-w-8"
                  [class]="n === p.page ? 'btn-primary' : 'btn-ghost'"
                  [attr.aria-current]="n === p.page ? 'page' : null"
                  (click)="n !== p.page && pagina.emit(n)"
                >
                  {{ n + 1 }}
                </button>
              }
            }
            <button
              type="button"
              class="btn-ghost btn-sm"
              aria-label="Página siguiente"
              [disabled]="p.page + 1 >= p.totalPages"
              (click)="pagina.emit(p.page + 1)"
            >
              <sf-icon name="chevronRight" class="size-4" />
            </button>
          </div>

          <label class="flex items-center gap-2">
            Por página
            <select
              class="input w-auto py-1"
              [value]="p.size"
              (change)="tamano.emit(+$any($event.target).value)"
            >
              @for (t of tamanos(); track t) {
                <option [value]="t" [selected]="t === p.size">{{ t }}</option>
              }
            </select>
          </label>
        </nav>
      }
    }
  `,
})
export class Paginator {
  readonly page = input.required<Page<unknown> | null>();
  readonly tamanos = input<number[]>([10, 20, 50]);
  /** Página pedida (base 0). */
  readonly pagina = output<number>();
  /** Nuevo tamaño de página; el padre debe volver a la página 0. */
  readonly tamano = output<number>();

  protected readonly desde = computed(() => {
    const p = this.page();
    return p ? p.page * p.size + 1 : 0;
  });
  protected readonly hasta = computed(() => {
    const p = this.page();
    return p ? p.page * p.size + p.content.length : 0;
  });

  /** Primera, última y las vecinas de la actual; el resto se resume con `…`. */
  protected readonly pasos = computed<Paso[]>(() => {
    const p = this.page();
    if (!p) return [];
    const total = p.totalPages;
    const pasos: Paso[] = [];
    for (let i = 0; i < total; i++) {
      if (i === 0 || i === total - 1 || Math.abs(i - p.page) <= 1) {
        pasos.push(i);
      } else if (pasos[pasos.length - 1] !== '…') {
        pasos.push('…');
      }
    }
    return pasos;
  });
}
