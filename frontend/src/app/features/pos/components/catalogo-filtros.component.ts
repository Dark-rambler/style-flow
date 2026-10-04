import { Component, model, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { FiltroTipo, OrdenCatalogo } from '../pos.models';

/** Lector de código de barras, buscador, filtro por tipo y orden del catálogo. */
@Component({
  selector: 'sf-catalogo-filtros',
  imports: [FormsModule],
  host: { class: 'flex flex-wrap gap-2' },
  template: `
    <label class="relative w-40 shrink-0 @max-lg:w-full">
      <svg
        class="pointer-events-none absolute top-1/2 left-3 size-4 -translate-y-1/2 text-slate-400"
        viewBox="0 0 24 24"
        fill="none"
        stroke="currentColor"
        stroke-width="2"
      >
        <path
          d="M4 8V5a1 1 0 0 1 1-1h3M16 4h3a1 1 0 0 1 1 1v3M20 16v3a1 1 0 0 1-1 1h-3M8 20H5a1 1 0 0 1-1-1v-3"
        />
      </svg>
      <input
        class="input pl-9"
        placeholder="Escanee el código"
        aria-label="Código de barras"
        [(ngModel)]="codigo"
        (keydown.enter)="enviarCodigo()"
      />
    </label>
    <div class="flex min-w-[16rem] flex-1 @max-lg:min-w-0">
      <input
        class="input min-w-0 rounded-r-none uppercase placeholder:normal-case"
        placeholder="Buscar…"
        aria-label="Buscar servicio o producto"
        [(ngModel)]="filtro"
      />
      <select
        class="border-y border-orange-500 bg-orange-400 px-2 text-sm font-medium text-white outline-none"
        aria-label="Tipo"
        [(ngModel)]="tipo"
      >
        <option value="TODOS">Todos</option>
        <option value="SERVICE">Servicios</option>
        <option value="PRODUCT">Productos</option>
      </select>
      <select
        class="rounded-r-lg bg-brand-600 px-2 text-sm font-medium text-white outline-none"
        aria-label="Ordenar"
        [(ngModel)]="orden"
      >
        <option value="nombre">A-Z</option>
        <option value="precio">Precio ↑</option>
        <option value="precioDesc">Precio ↓</option>
      </select>
    </div>
  `,
})
export class CatalogoFiltrosComponent {
  readonly filtro = model('');
  readonly tipo = model<FiltroTipo>('TODOS');
  readonly orden = model<OrdenCatalogo>('nombre');
  /** Código leído (Enter en el campo de código de barras). */
  readonly escanear = output<string>();

  protected readonly codigo = signal('');

  protected enviarCodigo(): void {
    const cod = this.codigo().trim();
    if (cod) this.escanear.emit(cod);
    this.codigo.set('');
  }
}
