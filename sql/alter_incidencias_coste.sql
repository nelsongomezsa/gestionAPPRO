-- Añadir campo coste_reparacion a Incidencias (ejecutar una sola vez)
ALTER TABLE Incidencias
    ADD COLUMN IF NOT EXISTS coste_reparacion DECIMAL(8,2) DEFAULT 0.00
        COMMENT 'Coste estimado o real de reparación de la incidencia';
