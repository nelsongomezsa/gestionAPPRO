-- ============================================================
--  GestionAp — Datos financieros de los 8 pisos de cartera
--  Cuotas calculadas con fórmula francesa exacta:
--    M = P · r·(1+r)^n / ((1+r)^n - 1)
--  Ejecutar sobre gestion_alquileres_pro (XAMPP MySQL)
-- ============================================================

USE gestion_alquileres_pro;

INSERT INTO DatosPiso
    (id_piso,
     precio_compra, itp_pagado, gastos_notaria, gastos_registro,
     coste_reforma, coste_mobiliario, otros_gastos_compra,
     tiene_hipoteca, importe_hipoteca, tipo_interes, plazo_anos, cuota_mensual_hipoteca,
     gasto_ibi_anual, gasto_comunidad_mensual, gasto_seguro_anual, gasto_mantenimiento_anual,
     valor_mercado_actual, fecha_compra, notas)
VALUES

-- ── PISO 1 ── Calle Mayor 15, Madrid — 5 hab — 4 alquiladas — 2.450 €/mes
--   Inversión total: 262.700 €  |  Capital propio: 108.700 €
--   Rent. bruta: 11.19 %  |  CF mensual estimado: ~1.383 €
(1,
 220000.00, 13200.00, 2500.00, 1500.00,
 18000.00, 7500.00, 0.00,
 1, 154000.00, 3.20, 25, 746.41,
 950.00, 140.00, 520.00, 700.00,
 255000.00, '2020-09-15',
 'Piso céntrico Madrid, zona Sol-Mayor. 5 hab reformadas, alta demanda. Hipoteca fija 3.20% TIN a 25 años. Plusvalía latente aprox 55.000€ desde compra.'),

-- ── PISO 2 ── Gran Vía 42, Madrid — 4 hab — 3 alquiladas — 1.670 €/mes
--   Inversión total: 195.600 €  |  Capital propio: 80.600 €
--   Rent. bruta: 10.23 %  |  CF mensual estimado: ~1.007 €
(2,
 165000.00, 9900.00, 2000.00, 1200.00,
 12000.00, 5500.00, 0.00,
 1, 115000.00, 3.00, 25, 545.34,
 720.00, 110.00, 420.00, 550.00,
 192000.00, '2021-03-20',
 'Gran Vía Madrid, zona premium. 4 hab, muy demandadas. Hipoteca fija 3.00% TIN a 25 años. Revalorización aprox +16% desde compra.'),

-- ── PISO 3 ── Calle Fuencarral 87, Madrid — 3 hab — 2 alquiladas — 900 €/mes
--   Inversión total: 118.480 €  |  Capital propio: 118.480 € (compra al contado)
--   Rent. bruta:  9.12 %  |  CF mensual estimado: ~811 €
(3,
 98000.00, 5880.00, 1300.00, 800.00,
 9500.00, 3500.00, 0.00,
 0, 0.00, 0.00, 0, 0.00,
 420.00, 65.00, 260.00, 380.00,
 118000.00, '2019-06-10',
 'Compra al contado con ahorros. Zona Fuencarral-Malasaña Madrid. 3 hab, menor coste → mejor ROE. Sin carga hipotecaria. Revalorización aprox +20% en 5 años.'),

-- ── PISO 4 ── Passeig de Gràcia 58, Barcelona — 6 hab — 5 alquiladas — 3.650 €/mes
--   Inversión total: 519.500 €  |  Capital propio: 219.500 €
--   Rent. bruta:  8.43 %  |  CF mensual estimado: ~1.872 €
(4,
 420000.00, 42000.00, 4800.00, 2700.00,
 35000.00, 15000.00, 0.00,
 1, 300000.00, 3.50, 30, 1347.13,
 1800.00, 250.00, 950.00, 1200.00,
 485000.00, '2021-07-01',
 'Passeig de Gràcia Barcelona, zona premium. 6 hab de lujo, 5 ocupadas. Hipoteca fija 3.50% TIN a 30 años. Mayor activo de la cartera. Plusvalía latente aprox 65.000€.'),

-- ── PISO 5 ── Carrer de Provença 144, Barcelona — 3 hab — 2 alquiladas — 1.200 €/mes
--   Inversión total: 223.300 €  |  Capital propio: 93.300 €
--   Rent. bruta:  6.45 %  |  CF mensual estimado: ~417 €
(5,
 185000.00, 18500.00, 2400.00, 1400.00,
 11000.00, 5000.00, 0.00,
 1, 130000.00, 3.20, 25, 630.08,
 780.00, 120.00, 480.00, 550.00,
 215000.00, '2022-02-14',
 'Carrer Provença, Eixample Barcelona. 2/3 hab alquiladas. Hipoteca fija 3.20% TIN a 25 años. Margen de mejora si se alquila 3ª hab (+600€/mes CF). Revalorización +16%.'),

-- ── PISO 6 ── Avenida del Puerto 34, Valencia — 4 hab — 2 alquiladas — 800 €/mes
--   Inversión total: 105.200 €  |  Capital propio: 50.200 €
--   Rent. bruta:  9.12 %  |  CF mensual estimado: ~396 €
(6,
 85000.00, 8500.00, 1200.00, 700.00,
 7000.00, 3000.00, 0.00,
 1, 55000.00, 2.80, 20, 299.55,
 350.00, 55.00, 230.00, 400.00,
 98000.00, '2020-11-08',
 'Avda del Puerto Valencia, zona costera. Solo 2/4 hab alquiladas — baja ocupación. Hipoteca fija 2.80% TIN a 20 años. Potencial alto si se optimiza ocupación. Revalorización +15%.'),

-- ── PISO 7 ── Calle Sierpes 22, Sevilla — 2 hab — 1 alquilada — 300 €/mes
--   Inversión total:  76.160 €  |  Capital propio: 76.160 € (compra al contado)
--   Rent. bruta:  4.73 %  |  CF mensual estimado: ~232 €
(7,
 62000.00, 4960.00, 950.00, 550.00,
 5500.00, 2200.00, 0.00,
 0, 0.00, 0.00, 0, 0.00,
 270.00, 45.00, 190.00, 300.00,
 72000.00, '2021-05-25',
 'Calle Sierpes Sevilla, centro histórico. Solo 1/2 hab alquilada — rentabilidad baja. Sin hipoteca. A REVISAR: aumentar ocupación o replantear estrategia. Revalorización +16%.'),

-- ── PISO 8 ── Gran Vía 18, Bilbao — 3 hab — 1 alquilada — 450 €/mes
--   Inversión total: 168.200 €  |  Capital propio: 68.200 €
--   Rent. bruta:  3.21 %  |  CF mensual estimado: -91 € (cash flow NEGATIVO)
(8,
 145000.00, 5800.00, 2000.00, 1200.00,
 10000.00, 4000.00, 0.00,
 1, 100000.00, 3.10, 30, 427.02,
 620.00, 95.00, 390.00, 520.00,
 162000.00, '2022-08-30',
 'Gran Vía Bilbao. Solo 1/3 hab alquilada — cash flow NEGATIVO (-91€/mes). Hipoteca fija 3.10% TIN a 30 años. ACCIÓN URGENTE: alquilar las 2 hab vacías para cubrir hipoteca y gastos. Revalorización +12%.')

ON DUPLICATE KEY UPDATE
    precio_compra             = VALUES(precio_compra),
    itp_pagado                = VALUES(itp_pagado),
    gastos_notaria            = VALUES(gastos_notaria),
    gastos_registro           = VALUES(gastos_registro),
    coste_reforma             = VALUES(coste_reforma),
    coste_mobiliario          = VALUES(coste_mobiliario),
    otros_gastos_compra       = VALUES(otros_gastos_compra),
    tiene_hipoteca            = VALUES(tiene_hipoteca),
    importe_hipoteca          = VALUES(importe_hipoteca),
    tipo_interes              = VALUES(tipo_interes),
    plazo_anos                = VALUES(plazo_anos),
    cuota_mensual_hipoteca    = VALUES(cuota_mensual_hipoteca),
    gasto_ibi_anual           = VALUES(gasto_ibi_anual),
    gasto_comunidad_mensual   = VALUES(gasto_comunidad_mensual),
    gasto_seguro_anual        = VALUES(gasto_seguro_anual),
    gasto_mantenimiento_anual = VALUES(gasto_mantenimiento_anual),
    valor_mercado_actual      = VALUES(valor_mercado_actual),
    fecha_compra              = VALUES(fecha_compra),
    notas                     = VALUES(notas);

-- ============================================================
--  VERIFICACIÓN — ejecutar después del INSERT para confirmar:
-- ============================================================
-- SELECT id_piso,
--        precio_compra + itp_pagado + gastos_notaria + gastos_registro +
--        coste_reforma + coste_mobiliario AS inversion_total,
--        cuota_mensual_hipoteca,
--        gasto_ibi_anual + gasto_comunidad_mensual*12 + gasto_seguro_anual + gasto_mantenimiento_anual AS gastos_anuales,
--        valor_mercado_actual, fecha_compra
-- FROM DatosPiso ORDER BY id_piso;

-- ============================================================
--  CÓMO IMPORTAR CON XAMPP:
--
--  1. Desde terminal (recomendado):
--
--       /Applications/XAMPP/xamppfiles/bin/mysql -u root gestion_alquileres_pro \
--           < sql/datos_pisos_financieros.sql
--
--  2. Desde phpMyAdmin:
--     → Abrir http://localhost/phpmyadmin
--     → Seleccionar base de datos "gestion_alquileres_pro"
--     → Pestaña "Importar" → elegir este fichero → Continuar
--
--  3. Comprobación rápida tras importar:
--
--       /Applications/XAMPP/xamppfiles/bin/mysql -u root gestion_alquileres_pro \
--           -e "SELECT id_piso, cuota_mensual_hipoteca, valor_mercado_actual FROM DatosPiso;"
-- ============================================================
