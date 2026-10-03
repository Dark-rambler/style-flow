import { Component, input, output, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { UsuarioResumen } from '../../../core/models';
import { MoneyPipe } from '../../../shared/money.pipe';
import { CambioLinea, CartLine, esCortesia, totalLinea } from '../pos.models';

/**
 * Detalle de la venta en curso. Solo muestra y emite lo que pide el usuario;
 * las validaciones (stock, topes de descuento) las aplica la página.
 */
@Component({
  selector: 'sf-carrito-tabla',
  imports: [FormsModule, MoneyPipe],
  host: { class: 'block min-h-40 flex-1 overflow-auto border-y border-slate-200' },
  template: `
    <table class="w-full text-sm @max-lg:block">
      <thead
        class="sticky top-0 z-1 bg-slate-100 text-xs font-semibold text-slate-500 uppercase @max-lg:hidden"
      >
        <tr>
          <th class="px-2 py-2 text-left">Detalle</th>
          <th class="w-14 px-1 py-2 text-left">Cant</th>
          <th class="w-18 px-1 py-2 text-left">P.U.</th>
          <th class="w-18 px-1 py-2 text-left">Desc</th>
          <th class="w-20 px-1 py-2 text-right">Total</th>
          <th class="w-16"><span class="sr-only">Acciones</span></th>
        </tr>
      </thead>
      <tbody class="@max-lg:block">
        @for (l of lineas(); track l.key) {
          <tr
            class="border-b border-slate-100 align-middle @max-lg:grid @max-lg:grid-cols-[repeat(3,minmax(0,1fr))_auto] @max-lg:items-end @max-lg:gap-x-2 @max-lg:gap-y-1 @max-lg:px-2 @max-lg:py-2"
          >
            <td class="px-2 py-1.5 @max-lg:col-span-3 @max-lg:p-0">
              <p class="text-xs font-medium text-slate-700 uppercase">{{ l.nombre }}</p>
              @if (l.tipo === 'SERVICE') {
                @if (editando() === l.key) {
                  <select
                    class="input mt-1 py-0.5 text-xs"
                    aria-label="Estilista"
                    [ngModel]="l.estilistaId"
                    (ngModelChange)="
                      cambiarEstilista.emit({ linea: l, valor: $event }); editando.set(null)
                    "
                  >
                    <option [ngValue]="null">— Estilista —</option>
                    @for (e of estilistas(); track e.id) {
                      <option [ngValue]="e.id">{{ e.name }}</option>
                    }
                  </select>
                } @else {
                  <p
                    class="text-[11px]"
                    [class]="l.estilistaId ? 'text-slate-400' : 'text-amber-600'"
                  >
                    {{ nombreEstilista(l.estilistaId) }}
                  </p>
                }
              }
            </td>
            <td class="px-1 @max-lg:p-0">
              <span
                class="mb-0.5 hidden text-[10px] font-semibold text-slate-400 uppercase @max-lg:block"
                >Cant</span
              >
              <input
                class="input px-1.5 py-1 text-right"
                type="number"
                min="1"
                step="1"
                aria-label="Cantidad"
                [ngModel]="l.cantidad"
                (ngModelChange)="cambiarCantidad.emit({ linea: l, valor: +$event })"
              />
            </td>
            <td class="px-1 @max-lg:p-0">
              <span
                class="mb-0.5 hidden text-[10px] font-semibold text-slate-400 uppercase @max-lg:block"
                >P.U.</span
              >
              <input
                class="input px-1.5 py-1 text-right"
                type="number"
                min="0"
                step="0.5"
                aria-label="Precio unitario"
                [ngModel]="l.precio"
                (ngModelChange)="cambiarPrecio.emit({ linea: l, valor: +$event })"
              />
            </td>
            <td class="px-1 @max-lg:p-0">
              <span
                class="mb-0.5 hidden text-[10px] font-semibold text-slate-400 uppercase @max-lg:block"
                >Desc</span
              >
              <input
                class="input px-1.5 py-1 text-right"
                type="number"
                min="0"
                step="0.5"
                aria-label="Descuento de la línea"
                [ngModel]="l.descuento"
                (ngModelChange)="cambiarDescuento.emit({ linea: l, valor: +$event })"
              />
            </td>
            <td
              class="px-1 text-right font-medium whitespace-nowrap @max-lg:col-start-4 @max-lg:row-start-2 @max-lg:self-center @max-lg:p-0"
            >
              {{ totalLinea(l) | money }}
            </td>
            <td class="px-1 @max-lg:col-start-4 @max-lg:row-start-1 @max-lg:self-start @max-lg:p-0">
              <div class="flex items-center justify-end gap-1.5 @max-lg:gap-3">
                <button
                  type="button"
                  class="text-red-500 hover:text-red-700"
                  title="Quitar"
                  aria-label="Quitar"
                  (click)="quitar.emit(l)"
                >
                  <svg
                    class="size-4"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M4 7h16M10 11v6M14 11v6M5 7l1 12a2 2 0 0 0 2 2h8a2 2 0 0 0 2-2l1-12M9 7V4h6v3"
                    />
                  </svg>
                </button>
                <button
                  type="button"
                  class="text-orange-500 hover:text-orange-700 disabled:invisible"
                  title="Cambiar estilista"
                  aria-label="Cambiar estilista"
                  [disabled]="l.tipo !== 'SERVICE'"
                  (click)="editando.set(editando() === l.key ? null : l.key)"
                >
                  <svg
                    class="size-4"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <path
                      d="M11 4H5a1 1 0 0 0-1 1v14a1 1 0 0 0 1 1h14a1 1 0 0 0 1-1v-6M17.5 2.5a2.1 2.1 0 0 1 3 3L12 14l-4 1 1-4Z"
                    />
                  </svg>
                </button>
                <button
                  type="button"
                  class="hover:text-brand-700"
                  [class]="esCortesia(l) ? 'text-brand-600' : 'text-slate-500'"
                  title="Cortesía (descuento total de la línea)"
                  aria-label="Cortesía"
                  [attr.aria-pressed]="esCortesia(l)"
                  (click)="cortesia.emit(l)"
                >
                  <svg
                    class="size-4"
                    viewBox="0 0 24 24"
                    fill="none"
                    stroke="currentColor"
                    stroke-width="2"
                  >
                    <rect x="3" y="8" width="18" height="4" rx="1" />
                    <path
                      d="M12 8v13M19 12v7a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2v-7M7.5 8a2.5 2.5 0 0 1 0-5C10 3 12 8 12 8s2-5 4.5-5a2.5 2.5 0 0 1 0 5"
                    />
                  </svg>
                </button>
              </div>
            </td>
          </tr>
        } @empty {
          <tr>
            <td colspan="6" class="p-8 text-center text-sm text-slate-400 @max-lg:block">
              Toque un servicio o producto para agregarlo.
            </td>
          </tr>
        }
      </tbody>
    </table>
  `,
})
export class CarritoTablaComponent {
  readonly lineas = input.required<CartLine[]>();
  readonly estilistas = input<UsuarioResumen[]>([]);

  readonly cambiarCantidad = output<CambioLinea<number>>();
  readonly cambiarPrecio = output<CambioLinea<number>>();
  readonly cambiarDescuento = output<CambioLinea<number>>();
  readonly cambiarEstilista = output<CambioLinea<number | null>>();
  readonly cortesia = output<CartLine>();
  readonly quitar = output<CartLine>();

  /** Línea cuyo estilista se está editando. */
  protected readonly editando = signal<number | null>(null);

  protected readonly totalLinea = totalLinea;
  protected readonly esCortesia = esCortesia;

  protected nombreEstilista(id: number | null): string {
    return this.estilistas().find((e) => e.id === id)?.name ?? 'Sin estilista';
  }
}
