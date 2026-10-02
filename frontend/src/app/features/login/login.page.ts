import { HttpErrorResponse } from '@angular/common/http';
import { Component, inject, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth/auth.service';
import { errorMessage } from '../../core/error.interceptor';

const NEGOCIO_KEY = 'stylo.negocio';

@Component({
  selector: 'sf-login-page',
  imports: [ReactiveFormsModule, RouterLink],
  template: `
    <div
      class="flex min-h-full items-center justify-center bg-gradient-to-br from-brand-50 to-slate-100 p-4"
    >
      <div class="w-full max-w-sm">
        <form class="card p-8" [formGroup]="form" (ngSubmit)="ingresar()">
          <div class="mb-6 flex flex-col items-center gap-2">
            <h1>
              <img src="/image/StyloFlow-logo.png" alt="Stylo Flow" class="h-16 w-auto" />
            </h1>
            <p class="text-sm text-slate-500">Ingrese con los datos de su peluquería</p>
          </div>
          <div class="flex flex-col gap-4">
            <label class="field">
              <span class="label">Negocio</span>
              <input
                class="input lowercase"
                formControlName="negocio"
                placeholder="código, p. ej. salon-bella"
                autocomplete="organization"
                autocapitalize="none"
                spellcheck="false"
              />
            </label>
            <label class="field">
              <span class="label">Usuario</span>
              <input
                class="input"
                formControlName="username"
                autocomplete="username"
                autocapitalize="none"
              />
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
          ¿Administra la plataforma?
          <a routerLink="/plataforma/login" class="text-slate-500 underline hover:text-slate-700"
            >Acceso de plataforma</a
          >
        </p>
        <div class="mt-8 flex flex-col items-center gap-1.5">
          <span class="text-[11px] tracking-wide text-slate-400 uppercase">Desarrollado por</span>
          <img src="/image/logoNC-claro.png" alt="Nexus Corp" class="h-7 w-auto" />
        </div>
      </div>
    </div>
  `,
})
export class LoginPage {
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);

  protected readonly loading = signal(false);
  // Si la sesión se cerró por un motivo (p. ej. negocio suspendido), se muestra al llegar al login
  protected readonly error = signal<string | null>(this.auth.motivoCierre());
  protected readonly form = inject(NonNullableFormBuilder).group({
    negocio: [leerNegocio(), Validators.required],
    username: ['', Validators.required],
    password: ['', Validators.required],
  });

  protected ingresar(): void {
    const { negocio, username, password } = this.form.getRawValue();
    const codigo = negocio.trim().toLowerCase();
    this.loading.set(true);
    this.error.set(null);
    this.auth.login(codigo, username, password).subscribe({
      next: () => {
        guardarNegocio(codigo);
        this.router.navigateByUrl(this.auth.homePath());
      },
      error: (err: HttpErrorResponse) => {
        this.loading.set(false);
        this.error.set(
          err.status === 401 ? 'Negocio, usuario o contraseña incorrectos' : errorMessage(err),
        );
      },
    });
  }
}

/** El código del negocio se recuerda en este equipo para no escribirlo cada vez. */
function leerNegocio(): string {
  try {
    return localStorage.getItem(NEGOCIO_KEY) ?? '';
  } catch {
    return '';
  }
}

function guardarNegocio(codigo: string): void {
  try {
    localStorage.setItem(NEGOCIO_KEY, codigo);
  } catch {
    // sin almacenamiento: se pedirá de nuevo la próxima vez
  }
}
