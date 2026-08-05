-- Tabla de log de actividad de usuarios
CREATE TABLE IF NOT EXISTS Actividad (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario  INT          NOT NULL,
    accion      VARCHAR(100) NOT NULL,
    descripcion TEXT,
    fecha       DATETIME     NOT NULL DEFAULT NOW(),
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_actividad_usuario ON Actividad(id_usuario, fecha);
