import { DIALOG_DATA, DialogRef } from '@angular/cdk/dialog';
import { Component, inject, OnInit, signal } from '@angular/core';
import { NonNullableFormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApiService } from '../../core/api.service';
import { ROLES, Rol, Usuario, UsuarioRequest } from '../../core/models';
import { ToastService } from '../../core/toast.service';
import { injectDialog, openDialog } from '../../shared/ui/dialog';

@Component({
  selector: 'sf-usuario-form',
  imports: [ReactiveFormsModule],
  template: `
    <form class="p-6" [formGroup]="form" (ngSubmit)="guardar()">
      <h2 class="text-lg font-semibold">{{ u ? 'Editar usuario' : 'Nuevo usuario' }}</h2>
      <div class="mt-4 grid grid-cols-2 gap-4">
        <label class="field col-span-2">
          <span class="label">Nombre completo *</span>
          <input class="input" formControlName="nombre" />
        </label>
        <label class="field">
          <span class="label">Usuario *</span>
          <input class="input" formControlName="username" autocomplete="off" />
        </label>
        <label class="field">
          <span class="label">{{ u ? 'Nueva contraseña' : 'Contraseña *' }}</span>
          <input
            class="input"
            type="password"
            formControlName="password"
            autocomplete="new-password"
            [placeholder]="u ? 'Dejar vacío para no cambiar' : ''"
          />
        </label>
        <label class="field">
          <span class="label">Rol *</span>
          <select class="input" formControlName="rol">
            @for (r of roles; track r.value) {
              <option [ngValue]="r.value">{{ r.label }}</option>
            }
          </select>
        </label>
        <label class="field">
          <span class="label">Teléfono</span>
          <input class="input" formControlName="telefono" />
        </label>
        @if (form.value.rol === 'ESTILISTA') {
          <label class="field">
            <span class="label">Comisión (%)</span>
            <input
              class="input"
              type="number"
              min="0"
              max="100"
              step="1"
              formControlName="comisionPorcentaje"
            />
          </label>
        }
        <label class="col-span-2 flex items-center gap-2 text-sm">
          <input type="checkbox" formControlName="activo" class="size-4 accent-brand-600" /> Activo
          (puede iniciar sesión)
        </label>
      </div>
      <div class="mt-6 flex justify-end gap-2">
        <button type="button" class="btn-secondary" (click)="ref.close()">Cancelar</button>
        <button type="submit" class="btn-primary" [disabled]="form.invalid || saving()">
          Guardar
        </button>
      </div>
    </form>
  `,
})
export class UsuarioFormDialog {
  protected readonly u = inject<Usuario | undefined>(DIALOG_DATA);
  protected readonly ref = inject<DialogRef<Usuario>>(DialogRef);
  private readonly api = inject(ApiService);
  private readonly toast = inject(ToastService);
  protected readonly roles = ROLES;
  protected readonly saving = signal(false);

  protected readonly form = inject(NonNullableFormBuilder).group({
    nombre: [this.u?.nombre ?? '', [Validators.required, Validators.maxLength(120)]],
    username: [
      this.u?.username ?? '',
      [Validators.required, Validators.minLength(3), Validators.pattern(/^[a-zA-Z0-9._-]+$/)],
    ],
    password: [
      '',
      this.u ? [Validators.minLength(6)] : [Validators.required, Validators.minLength(6)],
    ],
    rol: [this.u?.rol ?? ('ESTILISTA' as Rol), Validators.required],
    telefono: [this.u?.telefono ?? ''],
    comisionPorcentaje: [this.u?.comisionPorcentaje ?? 0, [Validators.min(0), Validators.max(100)]],
    activo: [this.u?.activo ?? true],
  });

  protected guardar(): void {
    this.saving.set(true);
    const v = this.form.getRawValue();
    const body: UsuarioRequest = { ...v, password: v.password || null };
    const req = this.u
      ? this.api.usuarios.actualizar(this.u.id, body)
      : this.api.usuarios.crear(body);
    req.subscribe({
      next: (r) => {
        this.toast.success('Usuario guardado');
        this.ref.close(r);
      },
      error: () => this.saving.set(false),
    });
  }
}

@Component({
  selector: 'sf-usuarios-page',
  template: `
    <div class="mb-6 flex items-center justify-between">
      <h1 class="page-title">Usuarios</h1>
      <button type="button" class="btn-primary" (click)="editar()">+ Nuevo usuario</button>
    </div>
    <div class="card overflow-x-auto">
      <table class="data-table">
        <thead>
          <tr>
            <th>Nombre</th>
            <th>Usuario</th>
            <th>Rol</th>
            <th class="text-right">Comisión</th>
            <th>Estado</th>
            <th></th>
          </tr>
        </thead>
        <tbody>
          @for (u of usuarios(); track u.id) {
            <tr [class.opacity-50]="!u.activo">
              <td class="font-medium">{{ u.nombre }}</td>
              <td class="text-slate-500">{{ u.username }}</td>
              <td>
                <span class="badge bg-brand-50 text-brand-700">{{ rolLabel(u.rol) }}</span>
              </td>
              <td class="text-right">
                {{ u.rol === 'ESTILISTA' ? u.comisionPorcentaje + '%' : '—' }}
              </td>
              <td>
                <span
                  class="badge"
                  [class]="
                    u.activo ? 'bg-emerald-100 text-emerald-700' : 'bg-slate-100 text-slate-500'
                  "
                  >{{ u.activo ? 'Activo' : 'Inactivo' }}</span
                >
              </td>
              <td class="text-right">
                <button type="button" class="btn-ghost btn-sm" (click)="editar(u)">Editar</button>
              </td>
            </tr>
          }
        </tbody>
      </table>
    </div>
  `,
})
export class UsuariosPage implements OnInit {
  private readonly api = inject(ApiService);
  private readonly dialog = injectDialog();
  protected readonly usuarios = signal<Usuario[]>([]);

  ngOnInit(): void {
    this.cargar();
  }

  protected rolLabel(r: Rol): string {
    return ROLES.find((x) => x.value === r)?.label ?? r;
  }

  protected editar(u?: Usuario): void {
    openDialog<Usuario, Usuario | undefined, UsuarioFormDialog>(
      this.dialog,
      UsuarioFormDialog,
      u,
    ).closed.subscribe((r) => r && this.cargar());
  }

  private cargar(): void {
    this.api.usuarios.listar().subscribe((u) => this.usuarios.set(u));
  }
}
