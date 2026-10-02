import { Component, input, model } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { CategoriaPos } from '../pos.models';

/** Menú lateral de categorías con buscador (pantallas anchas). */
@Component({
  selector: 'sf-categorias-lista',
  imports: [FormsModule],
  host: { class: 'card min-h-0 flex-col overflow-hidden' },
  template: `
    <nav class="contents" aria-label="Categorías">
      <div class="flex items-center gap-2 border-b border-slate-200 px-3 py-2">
        <svg
          class="size-4 shrink-0 text-slate-400"
          viewBox="0 0 24 24"
          fill="none"
          stroke="currentColor"
          stroke-width="2"
        >
          <circle cx="11" cy="11" r="7" />
          <path d="m20 20-3.5-3.5" />
        </svg>
        <input
          class="w-full bg-transparent py-1 text-sm outline-none placeholder:text-slate-400"
          placeholder="Buscar categoría…"
          [(ngModel)]="busqueda"
        />
      </div>
      <ul class="flex-1 overflow-auto">
        @for (c of categorias(); track c.id) {
          <li>
            <button
              type="button"
              class="flex w-full items-center justify-between border-b border-slate-100 px-4 py-3 text-left text-sm uppercase transition hover:bg-slate-50"
              [class]="
                seleccionada() === c.id
                  ? 'bg-brand-50 font-semibold text-brand-700'
                  : 'text-slate-600'
              "
              [attr.aria-current]="seleccionada() === c.id"
              (click)="seleccionada.set(c.id)"
            >
              {{ c.nombre }}
              @if (seleccionada() === c.id) {
                <span aria-hidden="true">›</span>
              }
            </button>
          </li>
        }
      </ul>
    </nav>
  `,
})
export class CategoriasListaComponent {
  readonly categorias = input.required<CategoriaPos[]>();
  readonly seleccionada = model.required<string>();
  readonly busqueda = model('');
}

/** Categorías como chips desplazables (pantallas angostas). */
@Component({
  selector: 'sf-categorias-chips',
  host: {
    class: '-mx-1 flex gap-2 overflow-x-auto px-1 pb-1',
    role: 'group',
    'aria-label': 'Categorías',
  },
  template: `
    @for (c of categorias(); track c.id) {
      <button
        type="button"
        class="shrink-0 rounded-full border px-3 py-1 text-xs font-medium uppercase transition"
        [class]="
          seleccionada() === c.id
            ? 'border-brand-500 bg-brand-600 text-white'
            : 'border-slate-300 bg-white text-slate-600 hover:bg-slate-50'
        "
        [attr.aria-pressed]="seleccionada() === c.id"
        (click)="seleccionada.set(c.id)"
      >
        {{ c.nombre }}
      </button>
    }
  `,
})
export class CategoriasChipsComponent {
  readonly categorias = input.required<CategoriaPos[]>();
  readonly seleccionada = model.required<string>();
}
