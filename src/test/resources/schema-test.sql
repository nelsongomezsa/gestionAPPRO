-- Esquema para tests de DAO contra H2 en memoria (modo MySQL).
-- Subconjunto de sql/base_datos.sql + sql/usuarios.sql + sql/notificaciones.sql,
-- adaptado para H2: sin CREATE DATABASE/USE, sin ENGINE/CHARSET, ENUM -> VARCHAR
-- (la capa Java ya valida los valores, no hace falta el ENUM de MySQL aquí).

CREATE TABLE Ciudades (
    id_ciudad   INT          PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL
);

CREATE TABLE Pisos (
    id_piso             INT          PRIMARY KEY AUTO_INCREMENT,
    direccion           VARCHAR(150) NOT NULL,
    numero_habitaciones INT          NOT NULL,
    id_ciudad           INT          NOT NULL,
    FOREIGN KEY (id_ciudad) REFERENCES Ciudades(id_ciudad)
);

CREATE TABLE Habitaciones (
    id_habitacion INT          PRIMARY KEY AUTO_INCREMENT,
    numero        INT          NOT NULL,
    precio        DECIMAL(8,2) NOT NULL,
    estado        VARCHAR(20)  NOT NULL DEFAULT 'disponible',
    id_piso       INT          NOT NULL,
    FOREIGN KEY (id_piso) REFERENCES Pisos(id_piso)
);

CREATE TABLE Inquilinos (
    id_inquilino INT          PRIMARY KEY AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL,
    apellidos    VARCHAR(100) NOT NULL,
    dni          VARCHAR(20)  NOT NULL UNIQUE,
    telefono     VARCHAR(20),
    email        VARCHAR(100)
);

CREATE TABLE Contratos (
    id_contrato    INT          PRIMARY KEY AUTO_INCREMENT,
    id_habitacion  INT          NOT NULL,
    id_inquilino   INT          NOT NULL,
    fecha_inicio   DATE         NOT NULL,
    fecha_fin      DATE         NOT NULL,
    precio_mensual DECIMAL(8,2) NOT NULL,
    FOREIGN KEY (id_habitacion) REFERENCES Habitaciones(id_habitacion),
    FOREIGN KEY (id_inquilino)  REFERENCES Inquilinos(id_inquilino)
);

CREATE TABLE Pagos (
    id_pago     INT          PRIMARY KEY AUTO_INCREMENT,
    id_contrato INT          NOT NULL,
    cantidad    DECIMAL(8,2) NOT NULL,
    metodo_pago VARCHAR(20)  NOT NULL DEFAULT 'transferencia',
    fecha_pago  DATE         NOT NULL,
    FOREIGN KEY (id_contrato) REFERENCES Contratos(id_contrato)
);

CREATE TABLE Incidencias (
    id_incidencia     INT          PRIMARY KEY AUTO_INCREMENT,
    id_habitacion     INT          NOT NULL,
    id_inquilino      INT          NOT NULL,
    descripcion       VARCHAR(255) NOT NULL,
    estado            VARCHAR(20)  NOT NULL DEFAULT 'pendiente',
    fecha             DATE         NOT NULL,
    coste_reparacion  DECIMAL(8,2) DEFAULT 0.00,
    FOREIGN KEY (id_habitacion) REFERENCES Habitaciones(id_habitacion),
    FOREIGN KEY (id_inquilino)  REFERENCES Inquilinos(id_inquilino)
);

CREATE TABLE Usuarios (
    id_usuario    INT           PRIMARY KEY AUTO_INCREMENT,
    nombre        VARCHAR(100)  NOT NULL,
    email         VARCHAR(100)  NOT NULL,
    password_hash VARCHAR(64)   NOT NULL,
    rol           VARCHAR(20)   NOT NULL DEFAULT 'usuario',
    activo        TINYINT(1)    NOT NULL DEFAULT 1,
    UNIQUE (email)
);

CREATE TABLE Notificaciones (
    id_notificacion INT AUTO_INCREMENT PRIMARY KEY,
    tipo            VARCHAR(50)  NOT NULL,
    titulo          VARCHAR(200) NOT NULL,
    descripcion     VARCHAR(4000),
    referencia      VARCHAR(100),
    fecha           TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    leida           TINYINT(1)   NOT NULL DEFAULT 0,
    id_usuario      INT          NOT NULL,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE
);

CREATE TABLE Actividad (
    id          INT AUTO_INCREMENT PRIMARY KEY,
    id_usuario  INT          NOT NULL,
    accion      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(4000),
    fecha       TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_usuario) REFERENCES Usuarios(id_usuario) ON DELETE CASCADE
);
