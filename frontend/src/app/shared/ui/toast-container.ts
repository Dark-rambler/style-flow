import { Component, inject } from '@angular/core';
import { ToastService } from '../../core/toast.service';

@Component({
  selector: 'sf-toast-container',
  template: `
    <div
      class="pointer-events-none fixed right-4 bottom-4 z-[2000] flex w-80 flex-col gap-2"
      aria-live="polite"
    >
      @for (t of toast.toasts(); track t.id) {
        <div
          class="pointer-events-auto flex items-start gap-3 rounded-lg px-4 py-3 text-sm text-white shadow-lg"
          [class.bg-emerald-600]="t.type === 'success'"
          [class.bg-red-600]="t.type === 'error'"
          [class.bg-slate-800]="t.type === 'info'"
          role="status"
        >
          <span class="flex-1">{{ t.message }}</span>
          <button
            type="button"
            class="opacity-70 hover:opacity-100"
            (click)="toast.dismiss(t.id)"
            aria-label="Cerrar"
          >
            ✕
          </button>
        </div>
      }
    </div>
  `,
})
export class ToastContainer {
  protected readonly toast = inject(ToastService);
}
