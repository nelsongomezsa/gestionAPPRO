\
-- PROYECTO: Gestión de Alquiler de Habitaciones
-- CIUDADES

INSERT INTO Ciudades (nombre) VALUES
    ('Madrid'),
    ('Barcelona'),
    ('Valencia');


-- PISOS

INSERT INTO Pisos (direccion, numero_habitaciones, id_ciudad) VALUES
    ('Calle Mayor 10, 3º A',   3, 1),
    ('Gran Via 20, 2º B',      4, 1),
    ('Passeig de Gràcia 45',   3, 2),
    ('Avenida del Puerto 12',  2, 3);


-- HABITACIONES

INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 450.00, 'alquilada',     1),
    (2, 420.00, 'disponible',    1),
    (3, 400.00, 'mantenimiento', 1),
    (1, 500.00, 'alquilada',     2),
    (2, 480.00, 'alquilada',     2),
    (3, 460.00, 'disponible',    2),
    (4, 470.00, 'disponible',    2),
    (1, 600.00, 'alquilada',     3),
    (2, 580.00, 'disponible',    3),
    (3, 590.00, 'alquilada',     3),
    (1, 380.00, 'alquilada',     4),
    (2, 370.00, 'disponible',    4);

-- INQUILINOS

INSERT INTO Inquilinos (nombre, apellidos, dni, telefono, email) VALUES
    ('Juan',   'Pérez García',    '12345678A', '612000001', 'juan.perez@email.com'),
    ('María',  'López Martínez',  '87654321B', '612000002', 'maria.lopez@email.com'),
    ('Carlos', 'Ruiz Fernández',  '11223344C', '612000003', 'carlos.ruiz@email.com'),
    ('Ana',    'Sánchez Torres',  '44332211D', '612000004', 'ana.sanchez@email.com'),
    ('Luis',   'Moreno Díaz',     '55667788E', '612000005', 'luis.moreno@email.com'),
    ('Sara',   'Jiménez Navarro', '88776655F', '612000006', 'sara.jimenez@email.com');


-- CONTRATOS

INSERT INTO Contratos (id_habitacion, id_inquilino, fecha_inicio, fecha_fin, precio_mensual) VALUES
    (1,  1, '2024-01-01', '2024-12-31', 450.00),
    (4,  2, '2024-02-01', '2025-01-31', 500.00),
    (5,  3, '2024-03-01', '2025-02-28', 480.00),
    (8,  4, '2024-01-15', '2025-01-14', 600.00),
    (10, 5, '2024-04-01', '2025-03-31', 590.00),
    (11, 6, '2023-06-01', '2024-05-31', 380.00);


-- PAGOS

INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    (1, 450.00, 'transferencia', '2024-01-05'),
    (1, 450.00, 'transferencia', '2024-02-05'),
    (1, 450.00, 'transferencia', '2024-03-05'),
    (2, 500.00, 'domiciliacion', '2024-02-10'),
    (2, 500.00, 'domiciliacion', '2024-03-10'),
    (3, 480.00, 'efectivo',      '2024-03-08'),
    (3, 480.00, 'efectivo',      '2024-04-08'),
    (4, 600.00, 'transferencia', '2024-01-20'),
    (4, 600.00, 'transferencia', '2024-02-20'),
    (5, 590.00, 'transferencia', '2024-04-05'),
    (6, 380.00, 'efectivo',      '2023-06-01'),
    (6, 380.00, 'efectivo',      '2023-07-01');

-- INCIDENCIAS

INSERT INTO Incidencias (id_habitacion, id_inquilino, descripcion, estado, fecha) VALUES
    (1,  1, 'Fuga de agua en el baño',           'resuelta',   '2024-01-20'),
    (4,  2, 'Calefacción no funciona',            'en_proceso', '2024-02-15'),
    (5,  3, 'Persiana rota en ventana',           'pendiente',  '2024-04-01'),
    (8,  4, 'Humedad en pared del dormitorio',    'pendiente',  '2024-03-10'),
    (10, 5, 'Cerradura de la puerta defectuosa',  'en_proceso', '2024-04-10');
