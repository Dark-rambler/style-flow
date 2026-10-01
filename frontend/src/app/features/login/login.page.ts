import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';

@Component({
  selector: 'sf-login-page',
  imports: [ReactiveFormsModule],
  template: `
    <div
      class="flex min-h-full items-center justify-center bg-gradient-to-br from-brand-50 to-slate-100 p-4"
    >
      <form class="card w-full max-w-sm p-8" [formGroup]="form" (ngSubmit)="ingresar()">
        <div class="mb-6 flex flex-col items-center gap-2">
          <span
            class="flex size-12 items-center justify-center rounded-xl bg-brand-600 text-xl font-bold text-white"
            >S</span
          >
          <h1 class="text-xl font-semibold text-slate-900">Stylo Flow</h1>
          <p class="text-sm text-slate-500">Ingrese con su usuario</p>
        </div>
        <div class="flex flex-col gap-4">
          <label class="field">
            <span class="label">Usuario</span>
            <input class="input" formControlName="username" autocomplete="username" />
          </label>
          <label class="field">
            <span class="label">Contraseña</span>
            <input
              class="input"
              type="password"
              formControlName="password"
              autocomplete="current-password"
            />
          </label>
          @if (error()) {
            <p class="rounded-lg bg-red-50 px-3 py-2 text-sm text-red-700" role="alert">
              {{ error() }}
            </p>
          }
          <button type="submit" class="btn-primary w-full" [disabled]="form.invalid || loading()">
            {{ loading() ? 'Ingresando…' : 'Ingresar' }}
          </button>
        </div>
      </form>
    </div>
  `,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  protected readonly error = signal<string | null>(null);
  protected readonly form = inject(NonNullableFormBuilder).group({
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected ingresar(): void {
    const { username, password } = this.form.getRawValue();
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(username, password).subscribe({
      next: () => this.router.navigateByUrl(this.auth.homePath()),
      error: (err) => {
        this.loading.set(false);
        this.error.set(
          err.status === 401
            ? 'Usuario o contraseña incorrectos'
            : 'No se pudo conectar con el servidor',
        );
      },
    });
  }
}
