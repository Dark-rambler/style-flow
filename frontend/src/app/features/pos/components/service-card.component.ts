import { Component, computed, effect, input, output, signal } from '@angular/core';
import { MoneyPipe } from '../../../shared/money.pipe';
import { ItemPos } from '../pos.models';

/** Tarjeta de un servicio o producto en la grilla del POS. */
@Component({
  selector: 'sf-service-card',
  imports: [MoneyPipe],
  template: `
    <div
      class="relative flex h-full flex-col rounded-xl border-2 bg-white p-2 m-1 shadow-sm transition"
      [class]="
        enCarrito() ? 'border-brand-300 bg-brand-50' : 'border-transparent hover:border-slate-200'
      "
      [class.opacity-50]="item().agotado"
    >
      <button
        type="button"
        class="flex flex-1 cursor-pointer flex-col text-left disabled:cursor-not-allowed"
        [disabled]="item().agotado"
        (click)="agregar.emit(item())"
      >
        <div class="aspect-4/3 w-full shrink-0 overflow-hidden rounded-lg bg-slate-100">
          <img
            class="size-full"
            [class]="usaImagen() ? 'object-cover object-center' : 'object-contain p-3'"
            [src]="usaImagen() ? item().imagenUrl : SIN_IMAGEN"
            alt=""
            loading="lazy"
            (error)="imagenFallida.set(true)"
          />
        </div>
        <span
          class="absolute top-3 right-3 rounded px-2 py-0.5 text-[11px] font-medium text-white"
          [class]="item().tipo === 'SERVICE' ? 'bg-orange-400' : 'bg-sky-500'"
        >
          {{ item().tipo === 'SERVICE' ? 'Servicio' : 'Producto' }}
        </span>
        <span class="mt-2 line-clamp-2 text-xs font-medium text-slate-700 uppercase">{{
          item().nombre
        }}</span>
        <span class="text-xs text-slate-500">{{ item().codigo }}</span>
        @if (item().stock !== undefined) {
          <span
            class="mt-1 self-start rounded px-1.5 py-0.5 text-[11px] font-medium"
            [class]="
              item().agotado
                ? 'bg-red-100 text-red-700'
                : item().stockBajo
                  ? 'bg-amber-100 text-amber-700'
                  : 'bg-slate-100 text-slate-600'
            "
          >
            {{ item().agotado ? 'Agotado' : 'Stock: ' + item().stock }}
          </span>
        }
        <span class="mt-auto pt-1 pr-9 text-sm font-bold text-slate-900">{{
          item().precio | money
        }}</span>
      </button>
      <button
        type="button"
        class="absolute right-2 bottom-2 flex size-8 items-center justify-center rounded-full border-2 border-brand-200 text-brand-600 hover:bg-brand-100"
        aria-label="Ver detalle"
        (click)="verDetalle.emit(item())"
      >
        <svg class="size-4" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <path d="M2 12s3.5-7 10-7 10 7 10 7-3.5 7-10 7S2 12 2 12Z" />
          <circle cx="12" cy="12" r="3" />
        </svg>
      </button>
    </div>
  `,
})
export class ServiceCardComponent {
  readonly item = input.required<ItemPos>();
  /** Si el ítem ya está en el carrito (se resalta la tarjeta). */
  readonly enCarrito = input(false);
  readonly agregar = output<ItemPos>();
  readonly verDetalle = output<ItemPos>();

  /** Imagen por defecto (en `public/`) si el ítem no tiene URL o esta no carga. */
  protected readonly SIN_IMAGEN = '/image/non-image.png';
  protected readonly imagenFallida = signal(false);
  protected readonly usaImagen = computed(() => !!this.item().imagenUrl && !this.imagenFallida());

  constructor() {
    // Al cambiar de ítem (o de URL) se vuelve a intentar cargar la imagen.
    effect(() => {
      this.item().imagenUrl;
      this.imagenFallida.set(false);
    });
  }
}
