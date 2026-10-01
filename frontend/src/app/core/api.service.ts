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

export interface RangoFechas {
  desde?: string | null;
  hasta?: string | null;
}

function params(values: Record<string, string | number | boolean | null | undefined>): HttpParams {
  let p = new HttpParams();
  for (const [k, v] of Object.entries(values)) {
    if (v !== null && v !== undefined && v !== '') p = p.set(k, String(v));
  }
  return p;
}

/** Cliente HTTP tipado de la API de Stylo Flow (un método por endpoint). */
@Injectable({ providedIn: 'root' })
export class ApiService {
  private readonly http = inject(HttpClient);

  // ---- Usuarios ----
  usuarios = {
    listar: () => this.http.get<Usuario[]>('/api/usuarios'),
    estilistas: () => this.http.get<UsuarioResumen[]>('/api/usuarios/estilistas'),
    crear: (body: UsuarioRequest) => this.http.post<Usuario>('/api/usuarios', body),
    actualizar: (id: number, body: UsuarioRequest) =>
      this.http.put<Usuario>(`/api/usuarios/${id}`, body),
    cambiarMiPassword: (password: string) =>
      this.http.put<void>('/api/usuarios/me/password', { password }),
  };

  // ---- Catálogo ----
  catalogo = {
    categorias: () => this.http.get<Categoria[]>('/api/catalogo/categorias'),
    crearCategoria: (body: Partial<Categoria>) =>
      this.http.post<Categoria>('/api/catalogo/categorias', body),
    actualizarCategoria: (id: number, body: Partial<Categoria>) =>
      this.http.put<Categoria>(`/api/catalogo/categorias/${id}`, body),
    eliminarCategoria: (id: number) => this.http.delete<void>(`/api/catalogo/categorias/${id}`),

    servicios: (soloActivos = false) =>
      this.http.get<Servicio[]>('/api/catalogo/servicios', { params: params({ soloActivos }) }),
    crearServicio: (body: ServicioRequest) =>
      this.http.post<Servicio>('/api/catalogo/servicios', body),
    actualizarServicio: (id: number, body: ServicioRequest) =>
      this.http.put<Servicio>(`/api/catalogo/servicios/${id}`, body),

    productos: (soloActivos = false) =>
      this.http.get<Producto[]>('/api/catalogo/productos', { params: params({ soloActivos }) }),
    stockBajo: () => this.http.get<Producto[]>('/api/catalogo/productos/stock-bajo'),
    crearProducto: (body: ProductoRequest) =>
      this.http.post<Producto>('/api/catalogo/productos', body),
    actualizarProducto: (id: number, body: ProductoRequest) =>
      this.http.put<Producto>(`/api/catalogo/productos/${id}`, body),
    ajustarStock: (id: number, cantidad: number) =>
      this.http.post<Producto>(`/api/catalogo/productos/${id}/ajuste-stock`, { cantidad }),
  };

  // ---- Clientes ----
  clientes = {
    buscar: (q: string, page = 0, size = 20) =>
      this.http.get<Page<Cliente>>('/api/clientes', { params: params({ q, page, size }) }),
    crear: (body: ClienteRequest) => this.http.post<Cliente>('/api/clientes', body),
    actualizar: (id: number, body: ClienteRequest) =>
      this.http.put<Cliente>(`/api/clientes/${id}`, body),
  };

  // ---- Ventas ----
  ventas = {
    crear: (body: VentaRequest) => this.http.post<Venta>('/api/ventas', body),
    buscar: (rango: RangoFechas & { clienteId?: number | null; page?: number; size?: number }) =>
      this.http.get<Page<VentaResumen>>('/api/ventas', { params: params({ ...rango }) }),
    obtener: (id: number) => this.http.get<Venta>(`/api/ventas/${id}`),
    anular: (id: number, motivo: string) =>
      this.http.post<Venta>(`/api/ventas/${id}/anular`, { motivo }),
  };

  // ---- Caja ----
  caja = {
    /** null cuando no hay caja abierta (204). */
    actual: () => this.http.get<Caja | null>('/api/caja/actual'),
    abrir: (montoInicial: number, observaciones?: string) =>
      this.http.post<Caja>('/api/caja/abrir', { montoInicial, observaciones }),
    cerrar: (efectivoContado: number, observaciones?: string) =>
      this.http.post<Caja>('/api/caja/cerrar', { efectivoContado, observaciones }),
    historial: (page = 0, size = 20) =>
      this.http.get<Page<Caja>>('/api/caja/historial', { params: params({ page, size }) }),
  };

  // ---- Reportes ----
  reportes = {
    resumen: (r: RangoFechas) =>
      this.http.get<Resumen>('/api/reportes/resumen', { params: params({ ...r }) }),
    ventasPorDia: (r: RangoFechas) =>
      this.http.get<VentaDia[]>('/api/reportes/ventas-por-dia', { params: params({ ...r }) }),
    estilistas: (r: RangoFechas) =>
      this.http.get<EstilistaTotal[]>('/api/reportes/estilistas', { params: params({ ...r }) }),
    misComisiones: (r: RangoFechas) =>
      this.http.get<EstilistaTotal[]>('/api/reportes/mis-comisiones', { params: params({ ...r }) }),
    top: (tipo: TipoItem, r: RangoFechas, limite = 10) =>
      this.http.get<ItemTop[]>('/api/reportes/top', { params: params({ tipo, limite, ...r }) }),
    ventasCsv: (r: RangoFechas): Observable<Blob> =>
      this.http.get('/api/reportes/ventas.csv', { params: params({ ...r }), responseType: 'blob' }),
  };

  // ---- Plataforma (solo SUPERADMIN) ----
  plataforma = {
    negocios: () => this.http.get<NegocioResumen[]>('/api/plataforma/negocios'),
    crearNegocio: (body: NegocioRequest) =>
      this.http.post<NegocioResumen>('/api/plataforma/negocios', body),
    cambiarEstado: (id: number, activo: boolean) =>
      this.http.put<void>(`/api/plataforma/negocios/${id}/estado`, { activo }),
  };

  // ---- Negocio ----
  negocio = {
    obtener: () => this.http.get<Negocio>('/api/negocio'),
    actualizar: (body: Negocio) => this.http.put<Negocio>('/api/negocio', body),
  };
}
