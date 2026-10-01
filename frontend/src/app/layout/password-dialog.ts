import { DialogRef } from '@angular/cdk/dialog';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../core/api.service';
import { ToastService } from '../core/toast.service';

@Component({
  selector: 'sf-password-dialog',
  imports: [ReactiveFormsModule],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">Cambiar contraseña</h2>
      <div class="mt-4 flex flex-col gap-4">
        <label class="field">
          <span class="label">Nueva contraseña</span>
          <input
            class="input"
            type="password"
            formControlName="password"
            autocomplete="new-password"
          />
        </label>
        <label class="field">
          <span class="label">Repetir contraseña</span>
          <input
            class="input"
            type="password"
            formControlName="repetir"
            autocomplete="new-password"
          />
        </label>
        @if (form.touched && form.value.password !== form.value.repetir) {
          <p class="error-text">Las contraseñas no coinciden</p>
        }
      </div>
      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button
          type="submit"
          class="btn-primary"
          [disabled]="form.invalid || form.value.password !== form.value.repetir || saving()"
        >
          Guardar
        </button>
      </div>
    </form>
  `,
})
export class PasswordDialog {
  protected readonly ref = inject(DialogRef);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  protected readonly saving = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    password: ['', [Validators.required, Validators.minLength(6)]],
    repetir: ['', Validators.required],
  });

  protected guardar(): void {
    this.saving.set(true);
    this.api.usuarios.cambiarMiPassword(this.form.getRawValue().password).subscribe({
      next: () => {
        this.toast.success('Contraseña actualizada');
        this.ref.close();
      },
      error: () => this.saving.set(false),
    });
  }
}
