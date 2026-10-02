// Tipos que reflejan los DTOs del backend (com.styloflow.*Dtos)

/** SUPERADMIN es de la plataforma: no pertenece a ningún negocio. */
export type Rol = 'ADMIN' | 'CAJERO' | 'ESTILISTA' | 'SUPERADMIN';
export type MetodoPago = 'EFECTIVO' | 'QR' | 'TARJETA' | 'TRANSFERENCIA';
export type TipoItem = 'SERVICIO' | 'PRODUCTO';

export const METODOS_PAGO: { value: MetodoPago; label: string }[] = [
  { value: 'EFECTIVO', label: 'Efectivo' },
  { value: 'QR', label: 'QR' },
  { value: 'TARJETA', label: 'Tarjeta' },
  { value: 'TRANSFERENCIA', label: 'Transferencia' },
];

export const ROLES: { value: Rol; label: string }[] = [
  { value: 'ADMIN', label: 'Administrador' },
  { value: 'CAJERO', label: 'Cajero' },
  { value: 'ESTILISTA', label: 'Estilista' },
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
  nombre: string;
  username: string;
  rol: Rol;
  telefono: string | null;
  comisionPorcentaje: number;
  activo: boolean;
}

export interface UsuarioRequest {
  nombre: string;
  username: string;
  password?: string | null;
  rol: Rol;
  telefono?: string | null;
  comisionPorcentaje?: number | null;
  activo?: boolean;
}

export interface UsuarioResumen {
  id: number;
  nombre: string;
  rol: Rol;
}

export interface NegocioInfo {
  codigo: string;
  nombre: string;
}

export interface LoginResponse {
  token: string;
  expiresAt: string;
  usuario: Usuario;
  negocio: NegocioInfo;
}

export interface PlataformaLoginResponse {
  token: string;
  expiresAt: string;
  nombre: string;
}

/** Negocio (tenant) visto desde la plataforma. */
export interface NegocioResumen {
  id: number;
  codigo: string;
  nombre: string;
  activo: boolean;
  createdAt: string;
  usuarios: number;
  ventas30d: number;
  total30d: number;
}

export interface NegocioRequest {
  nombre: string;
  codigo: string;
  nit?: string | null;
  telefono?: string | null;
  adminNombre: string;
  adminUsername: string;
  adminPassword: string;
  catalogoBase: boolean;
}

export interface Categoria {
  id: number;
  nombre: string;
  activo: boolean;
}

export interface Servicio {
  id: number;
  categoriaId: number;
  categoria: string;
  nombre: string;
  descripcion: string | null;
  duracionMin: number;
  precio: number;
  /** URL de la imagen que se muestra en el POS (aún no la envía el backend). */
  imagenUrl?: string | null;
  activo: boolean;
}

export type ServicioRequest = Omit<Servicio, 'id' | 'categoria'>;

export interface Producto {
  id: number;
  nombre: string;
  sku: string | null;
  precio: number;
  stock: number;
  stockMinimo: number;
  activo: boolean;
  stockBajo: boolean;
}

export type ProductoRequest = Omit<Producto, 'id' | 'stockBajo'>;

export interface Cliente {
  id: number;
  nombre: string;
  telefono: string | null;
  email: string | null;
  ciNit: string | null;
  notas: string | null;
  createdAt: string;
}

export type ClienteRequest = Omit<Cliente, 'id' | 'createdAt'>;

export interface ItemRequest {
  tipo: TipoItem;
  itemId: number;
  cantidad: number;
  precioUnitario?: number | null;
  /** Descuento de la línea (cortesía o rebaja); no puede superar precio × cantidad. */
  descuento?: number | null;
  estilistaId?: number | null;
}

export interface VentaRequest {
  clienteId?: number | null;
  items: ItemRequest[];
  descuento?: number | null;
  metodoPago: MetodoPago;
  montoRecibido?: number | null;
  observaciones?: string | null;
}

export interface VentaItem {
  id: number;
  tipo: TipoItem;
  itemId: number;
  descripcion: string;
  cantidad: number;
  precioUnitario: number;
  descuento: number;
  /** Neto de la línea: precio × cantidad − descuento. */
  subtotal: number;
  estilistaId: number | null;
  estilista: string | null;
}

export interface Venta {
  id: number;
  fecha: string;
  cajaId: number;
  cajero: string;
  clienteId: number | null;
  cliente: string | null;
  clienteCiNit: string | null;
  subtotal: number;
  descuento: number;
  total: number;
  iva: number;
  metodoPago: MetodoPago;
  montoRecibido: number;
  cambio: number;
  estado: 'COMPLETADA' | 'ANULADA';
  anuladaPor: string | null;
  anuladaEn: string | null;
  motivoAnulacion: string | null;
  observaciones: string | null;
  items: VentaItem[];
}

export interface VentaResumen {
  id: number;
  fecha: string;
  cajero: string;
  clienteId: number | null;
  cliente: string | null;
  total: number;
  metodoPago: MetodoPago;
  estado: 'COMPLETADA' | 'ANULADA';
  /** Solo se pueden anular ventas de la caja abierta. */
  cajaAbierta: boolean;
}

export interface TotalMetodo {
  metodo: MetodoPago;
  cantidad: number;
  total: number;
}

export interface Caja {
  id: number;
  estado: 'ABIERTA' | 'CERRADA';
  abiertaPor: string;
  abiertaEn: string;
  montoInicial: number;
  cerradaPor: string | null;
  cerradaEn: string | null;
  cantidadVentas: number;
  totalVentas: number;
  efectivoEsperado: number;
  efectivoContado: number | null;
  diferencia: number | null;
  porMetodo: TotalMetodo[];
  observaciones: string | null;
}

export interface Resumen {
  desde: string;
  hasta: string;
  cantidadVentas: number;
  totalVentas: number;
  ticketPromedio: number;
  totalDescuentos: number;
  totalIva: number;
  ventasAnuladas: number;
  porMetodo: TotalMetodo[];
}

export interface VentaDia {
  fecha: string;
  cantidad: number;
  total: number;
}

export interface EstilistaTotal {
  estilistaId: number;
  estilista: string;
  servicios: number;
  total: number;
  comisionPorcentaje: number;
  comision: number;
}

export interface ItemTop {
  id: number;
  nombre: string;
  cantidad: number;
  total: number;
}

export interface Negocio {
  /** Solo lectura: se usa al iniciar sesión. */
  codigo?: string;
  nombre: string;
  nit: string | null;
  direccion: string | null;
  telefono: string | null;
  moneda: string;
  simbolo: string;
  ivaPorcentaje: number;
  mensajeTicket: string | null;
}
