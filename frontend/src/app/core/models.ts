// Tipos que reflejan los DTOs del backend (records en `<modulo>/infrastructure/adapter/in/web/dto`)

/** SUPERADMIN es de la plataforma: no pertenece a ningún negocio. */
export type Rol = 'ADMIN' | 'CASHIER' | 'STYLIST' | 'SUPERADMIN';
export type MetodoPago = 'CASH' | 'QR' | 'CARD' | 'TRANSFER';
export type TipoItem = 'SERVICE' | 'PRODUCT';
export type EstadoVenta = 'COMPLETED' | 'VOIDED';
export type EstadoCaja = 'OPEN' | 'CLOSED';

export const METODOS_PAGO: { value: MetodoPago; label: string }[] = [
  { value: 'CASH', label: 'Efectivo' },
  { value: 'QR', label: 'QR' },
  { value: 'CARD', label: 'Tarjeta' },
  { value: 'TRANSFER', label: 'Transferencia' },
];

export const ROLES: { value: Rol; label: string }[] = [
  { value: 'ADMIN', label: 'Administrador' },
  { value: 'CASHIER', label: 'Cajero' },
  { value: 'STYLIST', label: 'Estilista' },
];

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Usuario {
  id: number;
  name: string;
  username: string;
  role: Rol;
  phone: string | null;
  commissionRate: number;
  active: boolean;
}

export interface UsuarioRequest {
  name: string;
  username: string;
  password?: string | null;
  role: Rol;
  phone?: string | null;
  commissionRate?: number | null;
  active?: boolean;
}

export interface UsuarioResumen {
  id: number;
  name: string;
  role: Rol;
}

export interface NegocioInfo {
  code: string;
  name: string;
}

export interface LoginResponse {
  token: string;
  expiresAt: string;
  user: Usuario;
  business: NegocioInfo;
}

export interface PlataformaLoginResponse {
  token: string;
  expiresAt: string;
  name: string;
}

/** Negocio (tenant) visto desde la plataforma. */
export interface NegocioResumen {
  id: number;
  code: string;
  name: string;
  active: boolean;
  createdAt: string;
  users: number;
  sales30d: number;
  total30d: number;
}

export interface NegocioRequest {
  name: string;
  code: string;
  taxId?: string | null;
  phone?: string | null;
  adminName: string;
  adminUsername: string;
  adminPassword: string;
  baseCatalog: boolean;
}

export interface Categoria {
  id: number;
  name: string;
  active: boolean;
}

export interface Servicio {
  id: number;
  categoryId: number;
  category: string;
  name: string;
  description: string | null;
  durationMinutes: number;
  price: number;
  /** URL de la imagen que se muestra en el POS (aún no la envía el backend). */
  imageUrl?: string | null;
  active: boolean;
}

export type ServicioRequest = Omit<Servicio, 'id' | 'category' | 'imageUrl'>;

export interface Producto {
  id: number;
  name: string;
  sku: string | null;
  price: number;
  stock: number;
  minStock: number;
  active: boolean;
  lowStock: boolean;
}

export type ProductoRequest = Omit<Producto, 'id' | 'lowStock'>;

export interface Cliente {
  id: number;
  name: string;
  phone: string | null;
  email: string | null;
  taxId: string | null;
  notes: string | null;
  createdAt: string;
}

export type ClienteRequest = Omit<Cliente, 'id' | 'createdAt'>;

export interface ItemRequest {
  type: TipoItem;
  itemId: number;
  quantity: number;
  unitPrice?: number | null;
  /** Descuento de la línea (cortesía o rebaja); no puede superar precio × cantidad. */
  discount?: number | null;
  stylistId?: number | null;
}

export interface VentaRequest {
  customerId?: number | null;
  items: ItemRequest[];
  discount?: number | null;
  paymentMethod: MetodoPago;
  amountReceived?: number | null;
  notes?: string | null;
}

export interface VentaItem {
  id: number;
  type: TipoItem;
  itemId: number;
  description: string;
  quantity: number;
  unitPrice: number;
  discount: number;
  /** Neto de la línea: precio × cantidad − descuento. */
  subtotal: number;
  stylistId: number | null;
  stylist: string | null;
}

export interface Venta {
  id: number;
  date: string;
  cashRegisterId: number;
  cashier: string;
  customerId: number | null;
  customer: string | null;
  customerTaxId: string | null;
  subtotal: number;
  discount: number;
  total: number;
  tax: number;
  paymentMethod: MetodoPago;
  amountReceived: number;
  change: number;
  status: EstadoVenta;
  voidedBy: string | null;
  voidedAt: string | null;
  voidReason: string | null;
  notes: string | null;
  items: VentaItem[];
}

export interface VentaResumen {
  id: number;
  date: string;
  cashier: string;
  customerId: number | null;
  customer: string | null;
  total: number;
  paymentMethod: MetodoPago;
  status: EstadoVenta;
  /** Solo se pueden anular ventas de la caja abierta. */
  cashRegisterOpen: boolean;
}

export interface TotalMetodo {
  method: MetodoPago;
  count: number;
  total: number;
}

export interface Caja {
  id: number;
  status: EstadoCaja;
  openedBy: string;
  openedAt: string;
  openingAmount: number;
  closedBy: string | null;
  closedAt: string | null;
  salesCount: number;
  salesTotal: number;
  expectedCash: number;
  countedCash: number | null;
  difference: number | null;
  byPaymentMethod: TotalMetodo[];
  notes: string | null;
}

export interface Resumen {
  from: string;
  to: string;
  salesCount: number;
  salesTotal: number;
  averageTicket: number;
  totalDiscounts: number;
  totalTax: number;
  voidedSales: number;
  byPaymentMethod: TotalMetodo[];
}

export interface VentaDia {
  date: string;
  count: number;
  total: number;
}

export interface EstilistaTotal {
  stylistId: number;
  stylist: string;
  services: number;
  total: number;
  commissionRate: number;
  commission: number;
}

export interface ItemTop {
  id: number;
  name: string;
  quantity: number;
  total: number;
}

export interface Negocio {
  /** Solo lectura: se usa al iniciar sesión. */
  code?: string;
  name: string;
  taxId: string | null;
  address: string | null;
  phone: string | null;
  currency: string;
  currencySymbol: string;
  taxRate: number;
  receiptMessage: string | null;
}
