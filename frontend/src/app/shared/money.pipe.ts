import { inject, Pipe, PipeTransform } from '@angular/core';
import { NegocioStore } from '../core/negocio.store';

const formatter = new Intl.NumberFormat('es-BO', {
  minimumFractionDigits: 2,
  maximumFractionDigits: 2,
});

/** `{{ 1234.5 | money }}` → `Bs 1.234,50` */
@Pipe({ name: 'money', pure: false })
export class MoneyPipe implements PipeTransform {
  private readonly store = inject(NegocioStore);

  transform(value: number | null | undefined): string {
    if (value === null || value === undefined) return '—';
    return `${this.store.simbolo()} ${formatter.format(value)}`;
  }
}
