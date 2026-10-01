import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/error.interceptor';

@Component({
  selector: 'sf-plataforma-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div class="flex min-h-full items-center justify-center bg-slate-900 p-4">
      <div class="w-full max-w-sm">
        <form class="card p-8" [formGroup]="form" (ngSubmit)="ingresar()">
          <div class="mb-6 flex flex-col items-center gap-2">
            <span
              class="flex size-12 items-center justify-center rounded-xl bg-slate-800 text-xl font-bold text-white"
              >S</span
            >
            <h1 class="text-xl font-semibold text-slate-900">Plataforma Stylo Flow</h1>
            <p class="text-sm text-slate-500">Administración de negocios</p>
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
        <p class="mt-4 text-center text-xs text-slate-400">
          <a routerLink="/login" class="underline hover:text-slate-200"
            >Volver al acceso de negocios</a
          >
        </p>
      </div>
    </div>
  `,
})
export class PlataformaLoginPage {
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
    this.auth.loginPlataforma(username.trim(), password).subscribe({
      next: () => this.router.navigateByUrl('/plataforma'),
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(err.status === 401 ? 'Usuario o contraseña incorrectos' : errorMessage(err));
      },
    });
  }
}
