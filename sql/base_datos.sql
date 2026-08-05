
-- PROYECTO: Gestión de Alquiler de Habitaciones


CREATE DATABASE IF NOT EXISTS gestion_alquileres
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_spanish_ci;

USE gestion_alquileres;


-- TABLA: Ciudades

CREATE TABLE Ciudades (
    id_ciudad   INT          PRIMARY KEY AUTO_INCREMENT,
    nombre      VARCHAR(100) NOT NULL
);


-- TABLA: Pisos
-- Un piso está en una ciudad (N:1)

CREATE TABLE Pisos (
    id_piso             INT          PRIMARY KEY AUTO_INCREMENT,
    direccion           VARCHAR(150) NOT NULL,
    numero_habitaciones INT          NOT NULL,
    id_ciudad           INT          NOT NULL,
    FOREIGN KEY (id_ciudad) REFERENCES Ciudades(id_ciudad)
);


-- TABLA: Habitaciones
-- Una habitación pertenece a un piso (N:1)
-- estado: disponible, alquilada, mantenimiento

CREATE TABLE Habitaciones (
    id_habitacion INT          PRIMARY KEY AUTO_INCREMENT,
    numero        INT          NOT NULL,
    precio        DECIMAL(8,2) NOT NULL,
    estado        ENUM('disponible','alquilada','mantenimiento') NOT NULL DEFAULT 'disponible',
    id_piso       INT          NOT NULL,
    FOREIGN KEY (id_piso) REFERENCES Pisos(id_piso)
);


-- TABLA: Inquilinos
-- Personas que alquilan habitaciones

CREATE TABLE Inquilinos (
    id_inquilino INT          PRIMARY KEY AUTO_INCREMENT,
    nombre       VARCHAR(100) NOT NULL,
    apellidos    VARCHAR(100) NOT NULL,
    dni          VARCHAR(20)  NOT NULL UNIQUE,
    telefono     VARCHAR(20),
    email        VARCHAR(100)
);


-- TABLA: Contratos
-- Vincula inquilino con habitación (relación se_alquilan)

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


-- TABLA: Pagos
-- Un contrato genera muchos pagos (relación generan)

CREATE TABLE Pagos (
    id_pago     INT          PRIMARY KEY AUTO_INCREMENT,
    id_contrato INT          NOT NULL,
    cantidad    DECIMAL(8,2) NOT NULL,
    metodo_pago ENUM('transferencia','efectivo','domiciliacion') NOT NULL DEFAULT 'transferencia',
    fecha_pago  DATE         NOT NULL,
    FOREIGN KEY (id_contrato) REFERENCES Contratos(id_contrato)
);


-- TABLA: Incidencias
-- Los inquilinos reportan incidencias en habitaciones

CREATE TABLE Incidencias (
    id_incidencia INT          PRIMARY KEY AUTO_INCREMENT,
    id_habitacion INT          NOT NULL,
    id_inquilino  INT          NOT NULL,
    descripcion   VARCHAR(255) NOT NULL,
    estado        ENUM('pendiente','en_proceso','resuelta') NOT NULL DEFAULT 'pendiente',
    fecha         DATE         NOT NULL,
    FOREIGN KEY (id_habitacion) REFERENCES Habitaciones(id_habitacion),
    FOREIGN KEY (id_inquilino)  REFERENCES Inquilinos(id_inquilino)
);
