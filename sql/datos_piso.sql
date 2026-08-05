-- Tabla de datos financieros detallados por piso
-- Ejecutar sobre la base de datos gestion_alquileres_pro

USE gestion_alquileres_pro;

CREATE TABLE IF NOT EXISTS DatosPiso (
    id                        INT PRIMARY KEY AUTO_INCREMENT,
    id_piso                   INT NOT NULL UNIQUE,

    -- Coste de adquisición
    precio_compra             DECIMAL(10,2) DEFAULT 0.00,
    itp_pagado                DECIMAL(10,2) DEFAULT 0.00,
    gastos_notaria            DECIMAL(10,2) DEFAULT 0.00,
    gastos_registro           DECIMAL(10,2) DEFAULT 0.00,
    coste_reforma             DECIMAL(10,2) DEFAULT 0.00,
    coste_mobiliario          DECIMAL(10,2) DEFAULT 0.00,
    otros_gastos_compra       DECIMAL(10,2) DEFAULT 0.00,

    -- Financiación
    tiene_hipoteca            TINYINT(1)    DEFAULT 0,
    importe_hipoteca          DECIMAL(10,2) DEFAULT 0.00,
    tipo_interes              DECIMAL(5,2)  DEFAULT 0.00,
    plazo_anos                INT           DEFAULT 0,
    cuota_mensual_hipoteca    DECIMAL(10,2) DEFAULT 0.00,

    -- Gastos operativos anuales
    gasto_ibi_anual           DECIMAL(10,2) DEFAULT 0.00,
    gasto_comunidad_mensual   DECIMAL(10,2) DEFAULT 0.00,
    gasto_seguro_anual        DECIMAL(10,2) DEFAULT 0.00,
    gasto_mantenimiento_anual DECIMAL(10,2) DEFAULT 0.00,

    -- Valor actual
    valor_mercado_actual      DECIMAL(10,2) DEFAULT 0.00,
    fecha_compra              DATE,
    notas                     TEXT,

    FOREIGN KEY (id_piso) REFERENCES Pisos(id_piso) ON DELETE CASCADE
);
