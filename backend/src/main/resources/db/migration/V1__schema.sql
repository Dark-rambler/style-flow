-- Stylo Flow: esquema inicial

CREATE TABLE negocio (
    id              SMALLINT PRIMARY KEY DEFAULT 1 CHECK (id = 1),
    nombre          VARCHAR(120) NOT NULL,
    nit             VARCHAR(30),
    direccion       VARCHAR(200),
    telefono        VARCHAR(30),
    moneda          VARCHAR(3)   NOT NULL DEFAULT 'BOB',
    simbolo         VARCHAR(5)   NOT NULL DEFAULT 'Bs',
    iva_porcentaje  NUMERIC(5,2) NOT NULL DEFAULT 13.00,
    mensaje_ticket  VARCHAR(250),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE usuarios (
    id                  BIGSERIAL PRIMARY KEY,
    nombre              VARCHAR(120) NOT NULL,
    username            VARCHAR(50)  NOT NULL UNIQUE,
    password_hash       VARCHAR(100) NOT NULL,
    rol                 VARCHAR(20)  NOT NULL CHECK (rol IN ('ADMIN', 'CAJERO', 'ESTILISTA')),
    telefono            VARCHAR(30),
    comision_porcentaje NUMERIC(5,2) NOT NULL DEFAULT 0 CHECK (comision_porcentaje BETWEEN 0 AND 100),
    activo              BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE categorias (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(80) NOT NULL UNIQUE,
    activo      BOOLEAN     NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE servicios (
    id            BIGSERIAL PRIMARY KEY,
    categoria_id  BIGINT        NOT NULL REFERENCES categorias (id),
    nombre        VARCHAR(120)  NOT NULL,
    descripcion   VARCHAR(500),
    duracion_min  INTEGER       NOT NULL CHECK (duracion_min > 0),
    precio        NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    activo        BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ   NOT NULL DEFAULT now(),
    UNIQUE (categoria_id, nombre)
);

CREATE TABLE productos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(120)  NOT NULL,
    sku         VARCHAR(50) UNIQUE,
    precio      NUMERIC(12,2) NOT NULL CHECK (precio >= 0),
    stock       INTEGER       NOT NULL DEFAULT 0 CHECK (stock >= 0),
    stock_minimo INTEGER      NOT NULL DEFAULT 0 CHECK (stock_minimo >= 0),
    activo      BOOLEAN       NOT NULL DEFAULT TRUE,
    created_at  TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ   NOT NULL DEFAULT now()
);

CREATE TABLE clientes (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(120) NOT NULL,
    telefono    VARCHAR(30),
    email       VARCHAR(120),
    ci_nit      VARCHAR(30),
    notas       VARCHAR(500),
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);
CREATE INDEX idx_clientes_nombre ON clientes (lower(nombre));

CREATE TABLE cajas (
    id                 BIGSERIAL PRIMARY KEY,
    estado             VARCHAR(10)   NOT NULL CHECK (estado IN ('ABIERTA', 'CERRADA')),
    abierta_por        BIGINT        NOT NULL REFERENCES usuarios (id),
    abierta_en         TIMESTAMPTZ   NOT NULL,
    monto_inicial      NUMERIC(12,2) NOT NULL CHECK (monto_inicial >= 0),
    cerrada_por        BIGINT REFERENCES usuarios (id),
    cerrada_en         TIMESTAMPTZ,
    efectivo_esperado  NUMERIC(12,2),
    efectivo_contado   NUMERIC(12,2),
    diferencia         NUMERIC(12,2),
    total_ventas       NUMERIC(12,2),
    observaciones      VARCHAR(500),
    created_at         TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at         TIMESTAMPTZ   NOT NULL DEFAULT now()
);
-- Solo puede existir una caja abierta a la vez
CREATE UNIQUE INDEX uq_caja_abierta ON cajas (estado) WHERE estado = 'ABIERTA';

CREATE TABLE ventas (
    id               BIGSERIAL PRIMARY KEY,
    fecha            TIMESTAMPTZ   NOT NULL,
    caja_id          BIGINT        NOT NULL REFERENCES cajas (id),
    cajero_id        BIGINT        NOT NULL REFERENCES usuarios (id),
    cliente_id       BIGINT REFERENCES clientes (id),
    subtotal         NUMERIC(12,2) NOT NULL,
    descuento        NUMERIC(12,2) NOT NULL DEFAULT 0,
    total            NUMERIC(12,2) NOT NULL,
    iva              NUMERIC(12,2) NOT NULL,
    metodo_pago      VARCHAR(15)   NOT NULL CHECK (metodo_pago IN ('EFECTIVO', 'QR', 'TARJETA', 'TRANSFERENCIA')),
    monto_recibido   NUMERIC(12,2) NOT NULL,
    cambio           NUMERIC(12,2) NOT NULL DEFAULT 0,
    estado           VARCHAR(12)   NOT NULL CHECK (estado IN ('COMPLETADA', 'ANULADA')),
    anulada_por      BIGINT REFERENCES usuarios (id),
    anulada_en       TIMESTAMPTZ,
    motivo_anulacion VARCHAR(250),
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    updated_at       TIMESTAMPTZ   NOT NULL DEFAULT now(),
    CHECK (descuento >= 0 AND descuento <= subtotal),
    CHECK (total = subtotal - descuento)
);
CREATE INDEX idx_ventas_fecha ON ventas (fecha);
CREATE INDEX idx_ventas_caja ON ventas (caja_id);
CREATE INDEX idx_ventas_cliente ON ventas (cliente_id);

CREATE TABLE venta_items (
    id               BIGSERIAL PRIMARY KEY,
    venta_id         BIGINT        NOT NULL REFERENCES ventas (id) ON DELETE CASCADE,
    tipo             VARCHAR(10)   NOT NULL CHECK (tipo IN ('SERVICIO', 'PRODUCTO')),
    servicio_id      BIGINT REFERENCES servicios (id),
    producto_id      BIGINT REFERENCES productos (id),
    estilista_id     BIGINT REFERENCES usuarios (id),
    descripcion      VARCHAR(150)  NOT NULL,
    cantidad         INTEGER       NOT NULL CHECK (cantidad > 0),
    precio_unitario  NUMERIC(12,2) NOT NULL CHECK (precio_unitario >= 0),
    subtotal         NUMERIC(12,2) NOT NULL,
    CHECK ((tipo = 'SERVICIO' AND servicio_id IS NOT NULL) OR (tipo = 'PRODUCTO' AND producto_id IS NOT NULL))
);
CREATE INDEX idx_items_venta ON venta_items (venta_id);
CREATE INDEX idx_items_estilista ON venta_items (estilista_id);
