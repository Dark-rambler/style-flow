import { Component, input, output } from '@angular/core';
import { ItemPos } from '../pos.models';
import { ServiceCardComponent } from './service-card.component';

/** Grilla desplazable con las tarjetas del catálogo. */
@Component({
  selector: 'sf-catalogo-grid',
  imports: [ServiceCardComponent],
  host: { class: 'block min-h-0 flex-1 overflow-auto pr-1 @max-4xl:max-h-[28rem]' },
  template: `
    <div class="grid grid-cols-[repeat(auto-fill,minmax(9.5rem,1fr))] gap-3">
      @for (it of items(); track it.tipo + it.id) {
        <sf-service-card
          [item]="it"
          [enCarrito]="enCarrito().has(it.tipo + it.id)"
          (agregar)="agregar.emit($event)"
          (verDetalle)="verDetalle.emit($event)"
        />
      } @empty {
        <p class="col-span-full p-6 text-center text-sm text-slate-500">
          No hay ítems que coincidan.
        </p>
      }
    </div>
  `,
})
export class CatalogoGridComponent {
  readonly items = input.required<ItemPos[]>();
  /** Claves `tipo + id` de los ítems que ya están en el carrito. */
  readonly enCarrito = input<ReadonlySet<string>>(new Set());
  readonly agregar = output<ItemPos>();
  readonly verDetalle = output<ItemPos>();
}
