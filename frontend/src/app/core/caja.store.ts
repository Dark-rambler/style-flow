import { computed, inject, Injectable, signal } from '@angular/core';
import { Observable, tap } from 'rxjs';
import { ApiService } from './api.service';
import { Caja } from './models';

/**
 * Caja (turno) abierta, compartida por la barra superior, Cobrar, Caja e Inicio.
 * Llame a `refrescar()` después de registrar o anular ventas para actualizar los totales.
 */
@Injectable({ providedIn: 'root' })
export class CajaStore {
  private readonly api = inject(ApiService);

  readonly caja = signal<Caja | null>(null);
  readonly cargada = signal(false);
  readonly abierta = computed(() => this.caja()?.estado === 'ABIERTA');

  refrescar(): void {
    this.api.caja.actual().subscribe((c) => {
      this.caja.set(c);
      this.cargada.set(true);
    });
  }

  abrir(montoInicial: number, observaciones?: string): Observable<Caja> {
    return this.api.caja.abrir(montoInicial, observaciones).pipe(tap((c) => this.caja.set(c)));
  }

  cerrar(efectivoContado: number, observaciones?: string): Observable<Caja> {
    return this.api.caja
      .cerrar(efectivoContado, observaciones)
      .pipe(tap(() => this.caja.set(null)));
  }
}
