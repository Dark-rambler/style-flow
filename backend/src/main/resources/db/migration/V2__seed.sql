-- Datos iniciales (el usuario admin se crea al arrancar: ver AdminInitializer)

INSERT INTO negocio (id, nombre, moneda, simbolo, iva_porcentaje, mensaje_ticket)
VALUES (1, 'Stylo Flow Peluquería', 'BOB', 'Bs', 13.00, '¡Gracias por su visita!');

INSERT INTO categorias (nombre) VALUES ('Cortes'), ('Color'), ('Tratamientos'), ('Peinados'), ('Uñas');

INSERT INTO servicios (categoria_id, nombre, duracion_min, precio)
SELECT c.id, s.nombre, s.duracion, s.precio
FROM (VALUES
        ('Cortes', 'Corte de dama', 45, 60.00),
        ('Cortes', 'Corte de caballero', 30, 35.00),
        ('Cortes', 'Corte infantil', 30, 25.00),
        ('Color', 'Tinte completo', 120, 180.00),
        ('Color', 'Mechas / balayage', 180, 350.00),
        ('Tratamientos', 'Hidratación profunda', 45, 90.00),
        ('Tratamientos', 'Keratina', 150, 400.00),
        ('Peinados', 'Brushing', 40, 50.00),
        ('Peinados', 'Peinado de evento', 60, 120.00),
        ('Uñas', 'Manicure', 40, 40.00),
        ('Uñas', 'Pedicure', 50, 50.00)
     ) AS s(categoria, nombre, duracion, precio)
JOIN categorias c ON c.nombre = s.categoria;

INSERT INTO productos (nombre, sku, precio, stock, stock_minimo) VALUES
    ('Shampoo profesional 500ml', 'SHA-500', 85.00, 20, 5),
    ('Acondicionador 500ml', 'ACO-500', 85.00, 15, 5),
    ('Cera para cabello', 'CER-100', 45.00, 30, 5);
