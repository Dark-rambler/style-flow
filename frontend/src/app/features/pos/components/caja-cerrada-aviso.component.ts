import { Component, output } from '@angular/core';

/** Aviso de caja cerrada con acceso directo para abrirla. */
@Component({
  selector: 'sf-caja-cerrada-aviso',
  host: {
    class:
      'flex shrink-0 flex-wrap items-center justify-between gap-2 rounded-lg border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-800',
  },
  template: `
    <span>La caja está cerrada. Ábrala para empezar a cobrar.</span>
    <button type="button" class="btn-primary btn-sm" (click)="abrir.emit()">Abrir caja</button>
  `,
})
export class CajaCerradaAvisoComponent {
  readonly abrir = output<void>();
}
