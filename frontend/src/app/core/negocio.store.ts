import { computed, inject, Injectable, signal } from '@angular/core';
import { ApiService } from './api.service';
import { Negocio } from './models';

/** Configuración del negocio cacheada (símbolo de moneda, IVA, datos del ticket). */
@Injectable({ providedIn: 'root' })
export class NegocioStore {
  private readonly api = inject(ApiService);

  readonly negocio = signal<Negocio | null>(null);
  readonly simbolo = computed(() => this.negocio()?.simbolo ?? 'Bs');

  cargar(): void {
    this.api.negocio.obtener().subscribe((n) => this.negocio.set(n));
  }
}
