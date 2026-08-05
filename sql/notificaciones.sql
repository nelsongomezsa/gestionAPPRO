-- Tabla de notificaciones persistentes
CREATE TABLE IF NOT EXISTS Notificaciones (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    tipo            VARCHAR(50)  NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    descripcion     TEXT,
    referencia      VARCHAR(100),
    fecha           DATETIME     NOT NULL DEFAULT NOW(),
    leida           TINYINT(1)   NOT NULL DEFAULT 0,
    id_usuario      INT          NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_notif_usuario_leida ON Notificaciones(id_usuario, leida);
CREATE INDEX IF NOT EXISTS idx_notif_referencia    ON Notificaciones(referencia, fecha);
