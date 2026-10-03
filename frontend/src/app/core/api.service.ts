import { HttpClient, HttpParams } from '@angular/common/http';
import { inject, Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import {
  Caja,
  Categoria,
  Cliente,
  ClienteRequest,
  EstilistaTotal,
  ItemTop,
  Negocio,
  NegocioRequest,
  NegocioResumen,
  Page,
  Producto,
  ProductoRequest,
  Resumen,
  Servicio,
  ServicioRequest,
  TipoItem,
  Usuario,
  UsuarioRequest,
  UsuarioResumen,
  Venta,
  VentaDia,
  VentaRequest,
  VentaResumen,
} from './models';

/** Rango de fechas (yyyy-MM-dd) tal como lo reciben los endpoints: `from` y `to`. */
export interface RangoFechas {
  from?: string | null;
  to?: string | null;
}

function params(values: Record<string, string | number | boolean | null | undefined>): HttpParams {
  let p = new HttpParams();
  for (const [k, v] of Object.entries(values)) {
    if (v !== null && v !== undefined && v !== '') p = p.set(k, String(v));
  }
  return p;
}

/** Cuerpo multipart con los campos sueltos (los enlaza `@ModelAttribute`) y la parte `image` opcional. */
function formData(
  values: Record<string, string | number | boolean | null | undefined>,
  imagen?: File | null,
): FormData {
  const fd = new FormData();
  for (const [k, v] of Object.entries(values)) {
    if (v !== null && v !== undefined) fd.append(k, String(v));
  }
  if (imagen) fd.append('image', imagen);
  return fd;
}

/** Cliente HTTP tipado de la API de Stylo Flow (un método por endpoint). */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  // ---- Usuarios ----
  usuarios = {
    listar: () => this.http.get<Usuario[]>('/api/users'),
    estilistas: () => this.http.get<UsuarioResumen[]>('/api/users/stylists'),
    crear: (body: UsuarioRequest) => this.http.post<Usuario>('/api/users', body),
    actualizar: (id: number, body: UsuarioRequest) =>
      this.http.put<Usuario>(`/api/users/${id}`, body),
    cambiarMiPassword: (password: string) =>
      this.http.put<void>('/api/users/me/password', { password }),
  };

  // ---- Catálogo ----
  catalogo = {
    categorias: () => this.http.get<Categoria[]>('/api/catalog/categories'),
    crearCategoria: (body: Partial<Categoria>) =>
      this.http.post<Categoria>('/api/catalog/categories', body),
    actualizarCategoria: (id: number, body: Partial<Categoria>) =>
      this.http.put<Categoria>(`/api/catalog/categories/${id}`, body),
    eliminarCategoria: (id: number) => this.http.delete<void>(`/api/catalog/categories/${id}`),

    servicios: (soloActivos = false) =>
      this.http.get<Servicio[]>('/api/catalog/services', {
        params: params({ activeOnly: false }),
      }),
    crearServicio: (body: ServicioRequest, imagen?: File | null) =>
      this.http.post<Servicio>('/api/catalog/services', formData({ ...body }, imagen)),
    actualizarServicio: (id: number, body: ServicioRequest) =>
      this.http.put<Servicio>(`/api/catalog/services/${id}`, body),

    productos: (soloActivos = false) =>
      this.http.get<Producto[]>('/api/catalog/products', {
        params: params({ activeOnly: false }),
      }),
    stockBajo: () => this.http.get<Producto[]>('/api/catalog/products/low-stock'),
    crearProducto: (body: ProductoRequest, imagen?: File | null) =>
      this.http.post<Producto>('/api/catalog/products', formData({ ...body }, imagen)),
    actualizarProducto: (id: number, body: ProductoRequest) =>
      this.http.put<Producto>(`/api/catalog/products/${id}`, body),
    ajustarStock: (id: number, cantidad: number) =>
      this.http.post<Producto>(`/api/catalog/products/${id}/stock-adjustment`, {
        quantity: cantidad,
      }),
  };

  // ---- Clientes ----
  clientes = {
    buscar: (q: string, page = 0, size = 20) =>
      this.http.get<Page<Cliente>>('/api/customers', { params: params({ q, page, size }) }),
    crear: (body: ClienteRequest) => this.http.post<Cliente>('/api/customers', body),
    actualizar: (id: number, body: ClienteRequest) =>
      this.http.put<Cliente>(`/api/customers/${id}`, body),
  };

  // ---- Ventas ----
  ventas = {
    crear: (body: VentaRequest) => this.http.post<Venta>('/api/sales', body),
    buscar: (rango: RangoFechas & { customerId?: number | null; page?: number; size?: number }) =>
      this.http.get<Page<VentaResumen>>('/api/sales', { params: params({ ...rango }) }),
    obtener: (id: number) => this.http.get<Venta>(`/api/sales/${id}`),
    anular: (id: number, motivo: string) =>
      this.http.post<Venta>(`/api/sales/${id}/void`, { reason: motivo }),
  };

  // ---- Caja ----
  caja = {
    /** null cuando no hay caja abierta (204). */
    actual: () => this.http.get<Caja | null>('/api/cash-register/current'),
    abrir: (openingAmount: number, notes?: string) =>
      this.http.post<Caja>('/api/cash-register/open', { openingAmount, notes }),
    cerrar: (countedCash: number, notes?: string) =>
      this.http.post<Caja>('/api/cash-register/close', { countedCash, notes }),
    historial: (page = 0, size = 20) =>
      this.http.get<Page<Caja>>('/api/cash-register/history', { params: params({ page, size }) }),
  };

  // ---- Reportes ----
  reportes = {
    resumen: (r: RangoFechas) =>
      this.http.get<Resumen>('/api/reports/summary', { params: params({ ...r }) }),
    ventasPorDia: (r: RangoFechas) =>
      this.http.get<VentaDia[]>('/api/reports/sales-by-day', { params: params({ ...r }) }),
    estilistas: (r: RangoFechas) =>
      this.http.get<EstilistaTotal[]>('/api/reports/stylists', { params: params({ ...r }) }),
    misComisiones: (r: RangoFechas) =>
      this.http.get<EstilistaTotal[]>('/api/reports/my-commissions', { params: params({ ...r }) }),
    top: (tipo: TipoItem, r: RangoFechas, limite = 10) =>
      this.http.get<ItemTop[]>('/api/reports/top', {
        params: params({ type: tipo, limit: limite, ...r }),
      }),
    ventasCsv: (r: RangoFechas): Observable<Blob> =>
      this.http.get('/api/reports/sales.csv', { params: params({ ...r }), responseType: 'blob' }),
  };

  // ---- Plataforma (solo SUPERADMIN) ----
  plataforma = {
    negocios: () => this.http.get<NegocioResumen[]>('/api/platform/businesses'),
    crearNegocio: (body: NegocioRequest) =>
      this.http.post<NegocioResumen>('/api/platform/businesses', body),
    cambiarEstado: (id: number, activo: boolean) =>
      this.http.put<void>(`/api/platform/businesses/${id}/status`, { active: activo }),
  };

  // ---- Negocio ----
  negocio = {
    obtener: () => this.http.get<Negocio>('/api/business'),
    actualizar: (body: Negocio) => this.http.put<Negocio>('/api/business', body),
  };
}
