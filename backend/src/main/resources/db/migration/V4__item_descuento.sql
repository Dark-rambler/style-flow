-- Descuento por línea (p. ej. cortesía). venta_items.subtotal pasa a ser neto: precio × cantidad − descuento.
ALTER TABLE venta_items ADD COLUMN descuento NUMERIC(12,2) NOT NULL DEFAULT 0 CHECK (descuento >= 0);
