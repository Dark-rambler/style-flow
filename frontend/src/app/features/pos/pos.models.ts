import { Producto, Servicio, TipoItem } from '../../core/models';

/** Ítem del catálogo tal como se muestra en la grilla del POS. */
export interface ItemPos {
  tipo: TipoItem;
  id: number;
  codigo: string;
  nombre: string;
  precio: number;
  categoria: string;
  agotado: boolean;
  /** Unidades disponibles (solo productos). */
  stock?: number;
  /** Stock en o por debajo del mínimo configurado (solo productos). */
  stockBajo?: boolean;
  imagenUrl?: string | null;
  ref: Servicio | Producto;
}

/** Línea de la venta en curso. */
export interface CartLine {
  key: number;
  tipo: TipoItem;
  itemId: number;
  nombre: string;
  precio: number;
  cantidad: number;
  /** Descuento en monto sobre la línea (precio × cantidad). */
  descuento: number;
  estilistaId: number | null;
  stock?: number;
}

/** Cambio pedido sobre una línea del carrito. */
export interface CambioLinea<T> {
  linea: CartLine;
  valor: T;
}

export interface CategoriaPos {
  id: string;
  nombre: string;
}

export type FiltroTipo = 'TODOS' | TipoItem;
export type OrdenCatalogo = 'nombre' | 'precio' | 'precioDesc';

export function round2(n: number): number {
  return Math.round(n * 100) / 100;
}

export function totalLinea(l: CartLine): number {
  return round2(l.precio * l.cantidad - l.descuento);
}

export function esCortesia(l: CartLine): boolean {
  return l.descuento > 0 && totalLinea(l) === 0;
}
