import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { Negocio } from '../../core/models';
import { NegocioStore } from '../../core/negocio.store';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'sf-configuracion-page',
  imports: [ReactiveFormsModule],
  template: `
    <h1 class="page-title mb-6">Configuración del negocio</h1>
    <form
      class="card grid max-w-2xl grid-cols-1 gap-4 p-6 sm:grid-cols-2"
      [formGroup]="form"
      (ngSubmit)="guardar()"
    >
      <label class="field sm:col-span-2"
        ><span class="label">Nombre del negocio *</span
        ><input class="input" formControlName="nombre"
      /></label>
      <label class="field"
        ><span class="label">NIT</span><input class="input" formControlName="nit"
      /></label>
      <label class="field"
        ><span class="label">Teléfono</span><input class="input" formControlName="telefono"
      /></label>
      <label class="field sm:col-span-2"
        ><span class="label">Dirección</span><input class="input" formControlName="direccion"
      /></label>
      <label class="field"
        ><span class="label">Moneda (ISO) *</span
        ><input class="input uppercase" maxlength="3" formControlName="moneda"
      /></label>
      <label class="field"
        ><span class="label">Símbolo *</span
        ><input class="input" maxlength="5" formControlName="simbolo"
      /></label>
      <label class="field"
        ><span class="label">IVA incluido en precios (%) *</span
        ><input
          class="input"
          type="number"
          min="0"
          max="100"
          step="0.01"
          formControlName="ivaPorcentaje"
      /></label>
      <label class="field sm:col-span-2"
        ><span class="label">Mensaje al pie del ticket</span
        ><input class="input" formControlName="mensajeTicket"
      /></label>
      <div class="flex justify-end sm:col-span-2">
        <button type="submit" class="btn-primary" [disabled]="form.invalid || saving()">
          Guardar cambios
        </button>
      </div>
    </form>
  `,
})
export class ConfiguracionPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly store = inject(NegocioStore);
  private readonly toast = inject(ToastService);
  protected readonly saving = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    nombre: ['', Validators.required],
    nit: [''],
    direccion: [''],
    telefono: [''],
    moneda: ['BOB', [Validators.required, Validators.minLength(3)]],
    simbolo: ['Bs', Validators.required],
    ivaPorcentaje: [13, [Validators.required, Validators.min(0), Validators.max(100)]],
    mensajeTicket: [''],
  });

  ngOnInit(): void {
    this.api.negocio.obtener().subscribe((n) => this.form.patchValue(n as never));
  }

  protected guardar(): void {
    this.saving.set(true);
    this.api.negocio.actualizar(this.form.getRawValue() as Negocio).subscribe({
      next: (n) => {
        this.store.negocio.set(n);
        this.toast.success('Configuración guardada');
        this.saving.set(false);
      },
      error: () => this.saving.set(false),
    });
  }
}
