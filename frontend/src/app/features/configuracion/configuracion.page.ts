import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { NegocioStore } from '../../core/negocio.store';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'sf-configuracion-page',
  imports: [ReactiveFormsModule],
  template: `
    <h1 class="page-title">Configuración del negocio</h1>
    @if (codigo()) {
      <p class="mt-1 mb-6 text-sm text-slate-500">
        Código de acceso:
        <code class="rounded bg-slate-100 px-1.5 py-0.5 text-slate-700">{{ codigo() }}</code>
        — sus usuarios lo escriben al iniciar sesión.
      </p>
    } @else {
      <div class="mb-6"></div>
    }
    <form
      class="card grid max-w-2xl grid-cols-1 gap-4 p-6 sm:grid-cols-2"
      [formGroup]="form"
      (ngSubmit)="guardar()"
    >
      <label class="field sm:col-span-2"
        ><span class="label">Nombre del negocio *</span><input class="input" formControlName="name"
      /></label>
      <label class="field"
        ><span class="label">NIT</span><input class="input" formControlName="taxId"
      /></label>
      <label class="field"
        ><span class="label">Teléfono</span><input class="input" formControlName="phone"
      /></label>
      <label class="field sm:col-span-2"
        ><span class="label">Dirección</span><input class="input" formControlName="address"
      /></label>
      <label class="field"
        ><span class="label">Moneda (ISO) *</span
        ><input class="input uppercase" maxlength="3" formControlName="currency"
      /></label>
      <label class="field"
        ><span class="label">Símbolo *</span
        ><input class="input" maxlength="5" formControlName="currencySymbol"
      /></label>
      <label class="field"
        ><span class="label">IVA incluido en precios (%) *</span
        ><input class="input" type="number" min="0" max="100" step="0.01" formControlName="taxRate"
      /></label>
      <label class="field sm:col-span-2"
        ><span class="label">Mensaje al pie del ticket</span
        ><input class="input" formControlName="receiptMessage"
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
  protected readonly codigo = signal('');

  protected readonly form = inject(NonNullableFormBuilder).group({
    name: ['', Validators.required],
    taxId: [''],
    address: [''],
    phone: [''],
    currency: ['BOB', [Validators.required, Validators.minLength(3)]],
    currencySymbol: ['Bs', Validators.required],
    taxRate: [13, [Validators.required, Validators.min(0), Validators.max(100)]],
    receiptMessage: [''],
  });

  ngOnInit(): void {
    this.api.negocio.obtener().subscribe((n) => {
      this.codigo.set(n.code ?? '');
      this.form.patchValue({
        ...n,
        taxId: n.taxId ?? '',
        address: n.address ?? '',
        phone: n.phone ?? '',
        receiptMessage: n.receiptMessage ?? '',
      });
    });
  }

  protected guardar(): void {
    this.saving.set(true);
    this.api.negocio.actualizar(this.form.getRawValue()).subscribe({
      next: (n) => {
        this.store.negocio.set(n);
        this.toast.success('Configuración guardada');
        this.saving.set(false);
      },
      error: () => this.saving.set(false),
    });
  }
}
