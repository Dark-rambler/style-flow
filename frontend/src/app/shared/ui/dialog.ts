import { Dialog, DialogRef } from '@angular/cdk/dialog';
import { ComponentType } from '@angular/cdk/portal';
import { inject } from '@angular/core';

/** Abre un diálogo CDK con el estilo de la app. Devuelve el DialogRef para escuchar `closed`. */
export function openDialog<R, D, C>(
  dialog: Dialog,
  component: ComponentType<C>,
  data?: D,
  width = '32rem',
): DialogRef<R, C> {
  return dialog.open<R, D, C>(component, {
    data,
    width,
    maxWidth: 'calc(100vw - 2rem)',
    panelClass: 'sf-dialog',
    backdropClass: 'cdk-overlay-dark-backdrop',
    autoFocus: 'first-tabbable',
  });
}

/** Atajo para usar dentro de componentes: `private dialog = injectDialog();` */
export const injectDialog = () => inject(Dialog);
