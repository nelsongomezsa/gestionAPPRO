-- ============================================================
-- GestionAp — Tabla de Usuarios del Sistema
-- Ejecutar en la base de datos gestion_alquileres
-- ============================================================

USE gestion_alquileres;

CREATE TABLE IF NOT EXISTS Usuarios (
    id_usuario    INT           NOT NULL AUTO_INCREMENT,
    nombre        VARCHAR(100)  NOT NULL,
    email         VARCHAR(100)  NOT NULL,
    password_hash VARCHAR(64)   NOT NULL COMMENT 'SHA-256 hex lowercase',
    rol           ENUM('admin','usuario') NOT NULL DEFAULT 'usuario',
    activo        TINYINT(1)    NOT NULL DEFAULT 1,
    PRIMARY KEY (id_usuario),
    UNIQUE KEY uq_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_spanish_ci;

-- Usuarios por defecto
-- Contraseña admin:    admin123
-- Contraseña usuario:  usuario123
INSERT INTO Usuarios (nombre, email, password_hash, rol) VALUES
('Administrador',  'admin@gestionap.com',   SHA2('admin123',   256), 'admin'),
('Usuario Demo',   'usuario@gestionap.com', SHA2('usuario123', 256), 'usuario')
ON DUPLICATE KEY UPDATE nombre = VALUES(nombre);
