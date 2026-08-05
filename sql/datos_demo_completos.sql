-- ============================================================
--  GestionAp — Datos demo completos
--  Base de datos: gestion_alquileres_pro
--  Fecha ref: 2026-05-01
--  150 pagos · 30 contratos · 25 inquilinos · 20 incidencias
-- ============================================================

USE gestion_alquileres_pro;

SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE Actividad;
TRUNCATE TABLE Notificaciones;
TRUNCATE TABLE Incidencias;
TRUNCATE TABLE Pagos;
TRUNCATE TABLE Contratos;
TRUNCATE TABLE Inquilinos;
TRUNCATE TABLE Habitaciones;
TRUNCATE TABLE Pisos;
TRUNCATE TABLE Ciudades;
TRUNCATE TABLE Usuarios;
SET FOREIGN_KEY_CHECKS = 1;

-- Añadir columna coste_reparacion si aún no existe
ALTER TABLE Incidencias
    ADD COLUMN IF NOT EXISTS coste_reparacion DECIMAL(8,2) DEFAULT 0.00;

-- ============================================================
--  CIUDADES (5)
-- ============================================================
INSERT INTO Ciudades (nombre) VALUES
    ('Madrid'),      -- id 1
    ('Barcelona'),   -- id 2
    ('Valencia'),    -- id 3
    ('Sevilla'),     -- id 4
    ('Bilbao');      -- id 5

-- ============================================================
--  PISOS (8): 3 Madrid · 2 Barcelona · 1 Valencia · 1 Sevilla · 1 Bilbao
-- ============================================================
INSERT INTO Pisos (direccion, numero_habitaciones, id_ciudad) VALUES
    ('Calle Mayor 15, 3º A',          5, 1),  -- id 1  Madrid
    ('Gran Vía 42, 2º B',             4, 1),  -- id 2  Madrid
    ('Calle Fuencarral 87, 1º C',     3, 1),  -- id 3  Madrid
    ('Passeig de Gràcia 58, 4º 1ª',   6, 2),  -- id 4  Barcelona
    ('Carrer de Provença 144, 2º 2ª', 3, 2),  -- id 5  Barcelona
    ('Avenida del Puerto 34, 3º B',   4, 3),  -- id 6  Valencia
    ('Calle Sierpes 22, 1º A',        2, 4),  -- id 7  Sevilla
    ('Gran Vía 18, 5º C',             3, 5);  -- id 8  Bilbao

-- ============================================================
--  HABITACIONES (30): 20 alquiladas · 6 disponibles · 4 mantenimiento
-- ============================================================
-- Piso 1 — Madrid Calle Mayor (5 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 550.00, 'alquilada',     1),  -- id 1
    (2, 620.00, 'alquilada',     1),  -- id 2
    (3, 580.00, 'alquilada',     1),  -- id 3
    (4, 700.00, 'alquilada',     1),  -- id 4
    (5, 490.00, 'disponible',    1);  -- id 5

-- Piso 2 — Madrid Gran Vía (4 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 480.00, 'alquilada',     2),  -- id 6
    (2, 540.00, 'alquilada',     2),  -- id 7
    (3, 650.00, 'alquilada',     2),  -- id 8
    (4, 510.00, 'mantenimiento', 2);  -- id 9

-- Piso 3 — Madrid Fuencarral (3 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 420.00, 'alquilada',     3),  -- id 10
    (2, 480.00, 'alquilada',     3),  -- id 11
    (3, 390.00, 'disponible',    3);  -- id 12

-- Piso 4 — Barcelona Gràcia (6 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 720.00, 'alquilada',     4),  -- id 13
    (2, 780.00, 'alquilada',     4),  -- id 14
    (3, 700.00, 'alquilada',     4),  -- id 15
    (4, 650.00, 'alquilada',     4),  -- id 16
    (5, 800.00, 'alquilada',     4),  -- id 17
    (6, 680.00, 'mantenimiento', 4);  -- id 18

-- Piso 5 — Barcelona Provença (3 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 580.00, 'alquilada',     5),  -- id 19
    (2, 620.00, 'alquilada',     5),  -- id 20
    (3, 550.00, 'disponible',    5);  -- id 21

-- Piso 6 — Valencia Puerto (4 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 380.00, 'alquilada',     6),  -- id 22
    (2, 420.00, 'alquilada',     6),  -- id 23
    (3, 360.00, 'disponible',    6),  -- id 24
    (4, 400.00, 'mantenimiento', 6);  -- id 25

-- Piso 7 — Sevilla Sierpes (2 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 300.00, 'alquilada',     7),  -- id 26
    (2, 320.00, 'disponible',    7);  -- id 27

-- Piso 8 — Bilbao Gran Vía (3 habs)
INSERT INTO Habitaciones (numero, precio, estado, id_piso) VALUES
    (1, 450.00, 'alquilada',     8),  -- id 28
    (2, 420.00, 'disponible',    8),  -- id 29
    (3, 480.00, 'mantenimiento', 8);  -- id 30

-- ============================================================
--  INQUILINOS (25)
-- ============================================================
INSERT INTO Inquilinos (nombre, apellidos, dni, telefono, email) VALUES
    ('Carlos',     'García Martínez',      '12345678A', '612 345 678', 'carlos.garcia@gmail.com'),
    ('María',      'López Sánchez',        '23456789B', '623 456 789', 'maria.lopez@hotmail.es'),
    ('Antonio',    'Rodríguez García',     '34567890C', '634 567 890', 'antonio.rodriguez@gmail.com'),
    ('Laura',      'Martínez Fernández',   '45678901D', '645 678 901', 'laura.martinez@outlook.es'),
    ('Manuel',     'González López',       '56789012E', '656 789 012', 'manuel.gonzalez@gmail.com'),
    ('Ana',        'Fernández Martínez',   '67890123F', '667 890 123', 'ana.fernandez@hotmail.com'),
    ('José',       'Sánchez Rodríguez',    '78901234G', '678 901 234', 'jose.sanchez@gmail.com'),
    ('Carmen',     'Gómez García',         '89012345H', '689 012 345', 'carmen.gomez@yahoo.es'),
    ('Francisco',  'Díaz López',           '90123456J', '690 123 456', 'francisco.diaz@gmail.com'),
    ('Isabel',     'Pérez Martínez',       '01234567K', '601 234 567', 'isabel.perez@hotmail.es'),
    ('Juan',       'Moreno Fernández',     '11234567L', '611 234 567', 'juan.moreno@gmail.com'),
    ('Elena',      'Jiménez González',     '22345678M', '622 345 678', 'elena.jimenez@outlook.com'),
    ('Miguel',     'Álvarez López',        '33456789N', '633 456 789', 'miguel.alvarez@gmail.com'),
    ('Sofía',      'Romero García',        '44567890P', '644 567 890', 'sofia.romero@gmail.com'),
    ('Pedro',      'Ruiz Martínez',        '55678901Q', '655 678 901', 'pedro.ruiz@hotmail.es'),
    ('Lucía',      'Torres Sánchez',       '66789012R', '666 789 012', 'lucia.torres@gmail.com'),
    ('Andrés',     'Flores Rodríguez',     '77890123S', '677 890 123', 'andres.flores@gmail.com'),
    ('Valentina',  'Ortega García',        '88901234T', '688 901 234', 'valentina.ortega@hotmail.com'),
    ('Alejandro',  'Morales Fernández',    '99012345V', '699 012 345', 'alejandro.morales@gmail.com'),
    ('Natalia',    'Castro López',         '00123456W', '600 123 456', 'natalia.castro@outlook.es'),
    ('David',      'Vargas Martínez',      '10234567X', '610 234 567', 'david.vargas@gmail.com'),
    ('Paula',      'Molina García',        '20345678Y', '620 345 678', 'paula.molina@gmail.com'),
    ('Roberto',    'Herrero López',        '30456789Z', '630 456 789', 'roberto.herrero@hotmail.es'),
    ('Sara',       'Iglesias Sánchez',     '40567890A', '640 567 890', 'sara.iglesias@gmail.com'),
    ('Fernando',   'Gutiérrez Rodríguez',  '50678901B', '650 678 901', 'fernando.gutierrez@gmail.com');

-- ============================================================
--  CONTRATOS (30)
--  Activos: 1-20  |  Vencen <30 días: 16-20  |  Históricos: 21-30
-- ============================================================

-- Contratos activos long-term (1-15, vencen 2026-07 a 2027-02)
INSERT INTO Contratos (id_habitacion, id_inquilino, fecha_inicio, fecha_fin, precio_mensual) VALUES
    ( 1,  1, '2025-01-01', '2026-09-30',  550.00),  -- id 1
    ( 2,  2, '2025-02-01', '2026-08-31',  620.00),  -- id 2
    ( 3,  3, '2025-03-01', '2026-11-30',  580.00),  -- id 3
    ( 4,  4, '2025-01-01', '2027-01-31',  700.00),  -- id 4
    ( 6,  5, '2025-04-01', '2026-10-31',  480.00),  -- id 5
    ( 7,  6, '2025-02-01', '2026-07-31',  540.00),  -- id 6
    ( 8,  7, '2024-11-01', '2026-06-30',  650.00),  -- id 7
    (10,  8, '2025-05-01', '2026-10-31',  420.00),  -- id 8
    (11,  9, '2025-01-01', '2026-12-31',  480.00),  -- id 9
    (13, 10, '2024-12-01', '2026-11-30',  720.00),  -- id 10
    (14, 11, '2025-01-01', '2026-09-30',  780.00),  -- id 11
    (15, 12, '2025-03-01', '2027-02-28',  700.00),  -- id 12
    (16, 13, '2025-02-01', '2026-07-31',  650.00),  -- id 13
    (17, 14, '2025-04-01', '2026-09-30',  800.00),  -- id 14
    (19, 15, '2025-06-01', '2026-11-30',  580.00);  -- id 15

-- Contratos activos que vencen en menos de 30 días (16-20, vencen 10-28 mayo 2026)
INSERT INTO Contratos (id_habitacion, id_inquilino, fecha_inicio, fecha_fin, precio_mensual) VALUES
    (20, 16, '2025-05-15', '2026-05-14',  620.00),  -- id 16  vence en 13 días
    (22, 17, '2025-05-20', '2026-05-19',  380.00),  -- id 17  vence en 18 días
    (23, 18, '2025-05-01', '2026-05-10',  420.00),  -- id 18  vence en  9 días
    (26, 19, '2025-05-28', '2026-05-27',  300.00),  -- id 19  vence en 26 días
    (28, 20, '2025-05-01', '2026-05-28',  450.00);  -- id 20  vence en 27 días

-- Contratos históricos finalizados (21-30)
INSERT INTO Contratos (id_habitacion, id_inquilino, fecha_inicio, fecha_fin, precio_mensual) VALUES
    ( 5, 21, '2024-01-01', '2024-12-31',  490.00),  -- id 21  hab 5 (disponible)
    ( 9, 22, '2023-06-01', '2024-05-31',  510.00),  -- id 22  hab 9 (mantenimiento)
    (12,  9, '2023-09-01', '2024-08-31',  390.00),  -- id 23  hab 12 (disponible)
    (21, 23, '2024-03-01', '2025-02-28',  550.00),  -- id 24  hab 21 (disponible)
    (27, 24, '2024-07-01', '2025-06-30',  320.00),  -- id 25  hab 27 (disponible)
    (29, 25, '2024-04-01', '2025-03-31',  420.00),  -- id 26  hab 29 (disponible)
    ( 1, 21, '2023-01-01', '2024-12-31',  520.00),  -- id 27  hab 1 inquilino anterior
    ( 6, 22, '2023-09-01', '2025-03-31',  460.00),  -- id 28  hab 6 inquilino anterior
    (24, 23, '2024-01-01', '2024-12-31',  360.00),  -- id 29  hab 24 (disponible)
    (30, 24, '2023-11-01', '2024-10-31',  480.00);  -- id 30  hab 30 (mantenimiento)

-- ============================================================
--  PAGOS (150 exactos)
--  Sección C: históricos  Nov 2024 – Mar 2025   →  25 pagos
--  Sección B: warm-up     Sep – Oct 2025         →  20 pagos
--  Sección A: recientes   Nov 2025 – Abr 2026    → 105 pagos
-- ============================================================

-- ── SECCIÓN C: contratos históricos (Nov 2024 – Mar 2025) ──

-- Noviembre 2024 (7 pagos)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    (21, 490.00, 'transferencia', '2024-11-05'),
    (24, 550.00, 'domiciliacion', '2024-11-01'),
    (25, 320.00, 'transferencia', '2024-11-07'),
    (26, 420.00, 'transferencia', '2024-11-04'),
    (27, 520.00, 'transferencia', '2024-11-06'),
    (28, 460.00, 'transferencia', '2024-11-05'),
    (29, 360.00, 'transferencia', '2024-11-03');

-- Diciembre 2024 (7 pagos)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    (21, 490.00, 'transferencia', '2024-12-04'),
    (24, 550.00, 'domiciliacion', '2024-12-01'),
    (25, 320.00, 'transferencia', '2024-12-06'),
    (26, 420.00, 'transferencia', '2024-12-03'),
    (27, 520.00, 'transferencia', '2024-12-05'),
    (28, 460.00, 'transferencia', '2024-12-04'),
    (29, 360.00, 'transferencia', '2024-12-02');

-- Enero 2025 (4 pagos — históricos)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    (24, 550.00, 'domiciliacion', '2025-01-01'),
    (25, 320.00, 'transferencia', '2025-01-08'),
    (26, 420.00, 'transferencia', '2025-01-05'),
    (28, 460.00, 'transferencia', '2025-01-06');

-- Febrero 2025 (4 pagos — históricos; C24 último mes)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    (24, 550.00, 'domiciliacion', '2025-02-01'),
    (25, 320.00, 'transferencia', '2025-02-05'),
    (26, 420.00, 'transferencia', '2025-02-04'),
    (28, 460.00, 'transferencia', '2025-02-05');

-- Marzo 2025 (3 pagos — C26 y C28 último mes)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    (25, 320.00, 'transferencia', '2025-03-06'),
    (26, 420.00, 'transferencia', '2025-03-04'),
    (28, 460.00, 'transferencia', '2025-03-05');

-- ── SECCIÓN B: warm-up Sep – Oct 2025 ──

-- Septiembre 2025 (8 pagos)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 1, 550.00, 'domiciliacion', '2025-09-01'),
    ( 2, 620.00, 'transferencia', '2025-09-04'),
    ( 4, 700.00, 'domiciliacion', '2025-09-01'),
    ( 7, 650.00, 'transferencia', '2025-09-05'),
    ( 9, 480.00, 'domiciliacion', '2025-09-01'),
    (10, 720.00, 'transferencia', '2025-09-03'),
    (11, 780.00, 'domiciliacion', '2025-09-01'),
    (15, 580.00, 'domiciliacion', '2025-09-01');

-- Octubre 2025 (12 pagos)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 3, 580.00, 'transferencia', '2025-10-06'),
    ( 5, 480.00, 'efectivo',      '2025-10-10'),
    ( 6, 540.00, 'transferencia', '2025-10-04'),
    ( 8, 420.00, 'transferencia', '2025-10-05'),
    (12, 700.00, 'transferencia', '2025-10-07'),
    (13, 650.00, 'transferencia', '2025-10-03'),
    (14, 800.00, 'efectivo',      '2025-10-15'),
    (16, 620.00, 'transferencia', '2025-10-06'),
    (17, 380.00, 'domiciliacion', '2025-10-01'),
    (18, 420.00, 'transferencia', '2025-10-08'),
    (19, 300.00, 'efectivo',      '2025-10-12'),
    (20, 450.00, 'domiciliacion', '2025-10-01');

-- ── SECCIÓN A: recientes Nov 2025 – Abr 2026 ──

-- Noviembre 2025 (20 pagos — todos los contratos activos)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 1, 550.00, 'domiciliacion', '2025-11-01'),
    ( 2, 620.00, 'transferencia', '2025-11-05'),
    ( 3, 580.00, 'transferencia', '2025-11-04'),
    ( 4, 700.00, 'domiciliacion', '2025-11-01'),
    ( 5, 480.00, 'efectivo',      '2025-11-12'),
    ( 6, 540.00, 'transferencia', '2025-11-03'),
    ( 7, 650.00, 'transferencia', '2025-11-06'),
    ( 8, 420.00, 'transferencia', '2025-11-05'),
    ( 9, 480.00, 'domiciliacion', '2025-11-01'),
    (10, 720.00, 'transferencia', '2025-11-04'),
    (11, 780.00, 'domiciliacion', '2025-11-01'),
    (12, 700.00, 'transferencia', '2025-11-07'),
    (13, 650.00, 'transferencia', '2025-11-05'),
    (14, 800.00, 'efectivo',      '2025-11-14'),
    (15, 580.00, 'domiciliacion', '2025-11-01'),
    (16, 620.00, 'transferencia', '2025-11-04'),
    (17, 380.00, 'domiciliacion', '2025-11-01'),
    (18, 420.00, 'transferencia', '2025-11-06'),
    (19, 300.00, 'efectivo',      '2025-11-10'),
    (20, 450.00, 'domiciliacion', '2025-11-01');

-- Diciembre 2025 (17 pagos — C3, C16, C19 no pagan ese mes)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 1, 550.00, 'domiciliacion', '2025-12-01'),
    ( 2, 620.00, 'transferencia', '2025-12-04'),
    -- C3 no paga en diciembre (gap para gráfico)
    ( 4, 700.00, 'domiciliacion', '2025-12-01'),
    ( 5, 480.00, 'efectivo',      '2025-12-09'),
    ( 6, 540.00, 'transferencia', '2025-12-03'),
    ( 7, 650.00, 'transferencia', '2025-12-05'),
    ( 8, 420.00, 'transferencia', '2025-12-04'),
    ( 9, 480.00, 'domiciliacion', '2025-12-01'),
    (10, 720.00, 'transferencia', '2025-12-03'),
    (11, 780.00, 'domiciliacion', '2025-12-01'),
    (12, 700.00, 'transferencia', '2025-12-06'),
    (13, 650.00, 'transferencia', '2025-12-04'),
    (14, 800.00, 'efectivo',      '2025-12-12'),
    (15, 580.00, 'domiciliacion', '2025-12-01'),
    -- C16 no paga en diciembre (gap)
    (17, 380.00, 'domiciliacion', '2025-12-01'),
    (18, 420.00, 'transferencia', '2025-12-05'),
    -- C19 no paga en diciembre (gap)
    (20, 450.00, 'domiciliacion', '2025-12-01');

-- Enero 2026 (20 pagos — mes fuerte, todos pagan)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 1, 550.00, 'domiciliacion', '2026-01-01'),
    ( 2, 620.00, 'transferencia', '2026-01-05'),
    ( 3, 580.00, 'transferencia', '2026-01-04'),
    ( 4, 700.00, 'domiciliacion', '2026-01-01'),
    ( 5, 480.00, 'efectivo',      '2026-01-11'),
    ( 6, 540.00, 'transferencia', '2026-01-03'),
    ( 7, 650.00, 'transferencia', '2026-01-06'),
    ( 8, 420.00, 'transferencia', '2026-01-05'),
    ( 9, 480.00, 'domiciliacion', '2026-01-01'),
    (10, 720.00, 'transferencia', '2026-01-04'),
    (11, 780.00, 'domiciliacion', '2026-01-01'),
    (12, 700.00, 'transferencia', '2026-01-07'),
    (13, 650.00, 'transferencia', '2026-01-05'),
    (14, 800.00, 'efectivo',      '2026-01-15'),
    (15, 580.00, 'domiciliacion', '2026-01-01'),
    (16, 620.00, 'transferencia', '2026-01-04'),
    (17, 380.00, 'domiciliacion', '2026-01-01'),
    (18, 420.00, 'transferencia', '2026-01-06'),
    (19, 300.00, 'efectivo',      '2026-01-13'),
    (20, 450.00, 'domiciliacion', '2026-01-01');

-- Febrero 2026 (20 pagos)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 1, 550.00, 'domiciliacion', '2026-02-01'),
    ( 2, 620.00, 'transferencia', '2026-02-05'),
    ( 3, 580.00, 'transferencia', '2026-02-04'),
    ( 4, 700.00, 'domiciliacion', '2026-02-01'),
    ( 5, 480.00, 'efectivo',      '2026-02-10'),
    ( 6, 540.00, 'transferencia', '2026-02-03'),
    ( 7, 650.00, 'transferencia', '2026-02-06'),
    ( 8, 420.00, 'transferencia', '2026-02-05'),
    ( 9, 480.00, 'domiciliacion', '2026-02-01'),
    (10, 720.00, 'transferencia', '2026-02-04'),
    (11, 780.00, 'domiciliacion', '2026-02-01'),
    (12, 700.00, 'transferencia', '2026-02-07'),
    (13, 650.00, 'transferencia', '2026-02-05'),
    (14, 800.00, 'efectivo',      '2026-02-14'),
    (15, 580.00, 'domiciliacion', '2026-02-01'),
    (16, 620.00, 'transferencia', '2026-02-04'),
    (17, 380.00, 'domiciliacion', '2026-02-01'),
    (18, 420.00, 'transferencia', '2026-02-06'),
    (19, 300.00, 'efectivo',      '2026-02-12'),
    (20, 450.00, 'domiciliacion', '2026-02-01');

-- Marzo 2026 (18 pagos — C5 y C18 no pagan ese mes)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 1, 550.00, 'domiciliacion', '2026-03-01'),
    ( 2, 620.00, 'transferencia', '2026-03-05'),
    ( 3, 580.00, 'transferencia', '2026-03-04'),
    ( 4, 700.00, 'domiciliacion', '2026-03-01'),
    -- C5 no paga en marzo (gap)
    ( 6, 540.00, 'transferencia', '2026-03-03'),
    ( 7, 650.00, 'transferencia', '2026-03-06'),
    ( 8, 420.00, 'transferencia', '2026-03-05'),
    ( 9, 480.00, 'domiciliacion', '2026-03-01'),
    (10, 720.00, 'transferencia', '2026-03-04'),
    (11, 780.00, 'domiciliacion', '2026-03-01'),
    (12, 700.00, 'transferencia', '2026-03-07'),
    (13, 650.00, 'transferencia', '2026-03-05'),
    (14, 800.00, 'efectivo',      '2026-03-12'),
    (15, 580.00, 'domiciliacion', '2026-03-01'),
    (16, 620.00, 'transferencia', '2026-03-04'),
    (17, 380.00, 'domiciliacion', '2026-03-01'),
    -- C18 no paga en marzo (gap)
    (19, 300.00, 'efectivo',      '2026-03-10'),
    (20, 450.00, 'domiciliacion', '2026-03-01');

-- Abril 2026 (10 pagos — mitad de contratos, el mes aún no ha cerrado)
INSERT INTO Pagos (id_contrato, cantidad, metodo_pago, fecha_pago) VALUES
    ( 1, 550.00, 'domiciliacion', '2026-04-01'),
    ( 2, 620.00, 'transferencia', '2026-04-04'),
    ( 4, 700.00, 'domiciliacion', '2026-04-01'),
    ( 6, 540.00, 'transferencia', '2026-04-03'),
    ( 9, 480.00, 'domiciliacion', '2026-04-01'),
    (11, 780.00, 'domiciliacion', '2026-04-01'),
    (13, 650.00, 'transferencia', '2026-04-04'),
    (15, 580.00, 'domiciliacion', '2026-04-01'),
    (17, 380.00, 'domiciliacion', '2026-04-01'),
    (20, 450.00, 'domiciliacion', '2026-04-01');

-- ============================================================
--  INCIDENCIAS (20): 5 pendientes · 7 en proceso · 8 resueltas
-- ============================================================
INSERT INTO Incidencias (id_habitacion, id_inquilino, descripcion, estado, fecha, coste_reparacion) VALUES
    -- Resueltas (8)
    ( 1,  1, 'Fuga de agua bajo el lavabo del baño',                        'resuelta',   '2025-11-15',  250.00),
    ( 2,  2, 'Enchufe de la cocina quemado, no da corriente',               'resuelta',   '2025-10-08',  180.00),
    ( 4,  4, 'Radiador del dormitorio no calienta nada',                    'resuelta',   '2025-12-20',  450.00),
    ( 6,  5, 'Mancha de humedad en la pared del salón, pintura levantada',  'resuelta',   '2026-01-10',  320.00),
    ( 8,  7, 'Puerta de entrada no cierra bien, cerradura dañada',          'resuelta',   '2025-09-22',   80.00),
    (13, 10, 'Grieta en techo del dormitorio con filtración de agua',       'resuelta',   '2026-02-15',  680.00),
    (14, 11, 'Desagüe de la ducha atascado, agua no baja',                  'resuelta',   '2025-11-30',  120.00),
    (17, 14, 'Ventana del salón rota, no cierra herméticamente',            'resuelta',   '2026-03-05',  150.00),
    -- En proceso (7)
    ( 3,  3, 'Calefacción central no funciona desde hace 3 días',           'en_proceso', '2026-02-10',  380.00),
    ( 7,  6, 'Avería en caldera, sin agua caliente',                        'en_proceso', '2026-01-25', 1200.00),
    (11,  9, 'Desagüe de lavabo atascado en baño principal',                'en_proceso', '2026-03-18',  220.00),
    (15, 12, 'Cortocircuito en cuadro eléctrico, sin luz en habitación',   'en_proceso', '2026-02-28',  550.00),
    (16, 13, 'Pintura descascarillada en techo y paredes del dormitorio',   'en_proceso', '2026-04-02',   95.00),
    (19, 15, 'Cerradura de la puerta exterior bloqueada',                   'en_proceso', '2026-03-25',   70.00),
    (22, 17, 'Grieta en pared medianera, se oyen ruidos del vecino',        'en_proceso', '2026-04-10',  850.00),
    -- Pendientes (5)
    (10,  8, 'Grifo de la cocina gotea continuamente',                      'pendiente',  '2026-04-20',   50.00),
    (20, 16, 'Enchufe del dormitorio ha dado chispa y huele a quemado',     'pendiente',  '2026-04-18',   90.00),
    (23, 18, 'Mancha de humedad en el techo del baño, posible filtración',  'pendiente',  '2026-04-25', 1500.00),
    (26, 19, 'Persiana exterior rota, no sube ni baja',                     'pendiente',  '2026-04-28',  200.00),
    (28, 20, 'Calefacción no enciende, termostato no responde',             'pendiente',  '2026-04-29',  750.00);

-- ============================================================
--  USUARIOS (3)
-- ============================================================
INSERT INTO Usuarios (nombre, email, password_hash, rol, activo) VALUES
    ('Administrador',   'admin@gestionap.com',  SHA2('admin123',   256), 'admin',   1),
    ('Gestor Demo',     'gestor@gestionap.com', SHA2('gestor123',  256), 'usuario', 1),
    ('Nelson Gómez',    'nelson@gestionap.com', SHA2('nelson123',  256), 'admin',   1);

-- ============================================================
--  FIN — verificación rápida
-- ============================================================
SELECT 'Ciudades'    AS tabla, COUNT(*) AS total FROM Ciudades
UNION ALL SELECT 'Pisos',       COUNT(*) FROM Pisos
UNION ALL SELECT 'Habitaciones',COUNT(*) FROM Habitaciones
UNION ALL SELECT 'Inquilinos',  COUNT(*) FROM Inquilinos
UNION ALL SELECT 'Contratos',   COUNT(*) FROM Contratos
UNION ALL SELECT 'Pagos',       COUNT(*) FROM Pagos
UNION ALL SELECT 'Incidencias', COUNT(*) FROM Incidencias
UNION ALL SELECT 'Usuarios',    COUNT(*) FROM Usuarios;

-- ALTER TABLE recordatorio (idempotente)
ALTER TABLE Incidencias
    ADD COLUMN IF NOT EXISTS coste_reparacion DECIMAL(8,2) DEFAULT 0.00;
