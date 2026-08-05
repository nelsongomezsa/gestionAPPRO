USE gestion_alquileres;


CONSULTAS PROYECTO ALQUILERES


1. Ver todos los pisos con su ciudad
SELECT Pisos.id_piso, Pisos.direccion, Ciudades.nombre_ciudad
FROM Pisos
INNER JOIN Ciudades ON Pisos.id_ciudad = Ciudades.id_ciudad;
2. Ver habitaciones con su piso
SELECT Habitaciones.id_habitacion, Pisos.direccion
FROM Habitaciones
INNER JOIN Pisos ON Habitaciones.id_piso = Pisos.id_piso;

3. Ver inquilinos con sus contratos
SELECT Inquilinos.nombre_cliente, Inquilinos.apellido_cliente, Contratos.fecha_inicio, Contratos.fecha_fin
FROM Inquilinos
INNER JOIN Contratos ON Inquilinos.id_cliente = Contratos.id_cliente;

4. Ver pagos con información del inquilino
SELECT Inquilinos.nombre_cliente, Pagos.cantidad, Pagos.fecha_pago
FROM Pagos
INNER JOIN Contratos ON Pagos.id_contrato = Contratos.id_contrato
INNER JOIN Inquilinos ON Contratos.id_cliente = Inquilinos.id_cliente;

5. Ver incidencias con habitaciones
SELECT Incidencias.descripcion, Incidencias.estado, Habitaciones.id_habitacion
FROM Incidencias
INNER JOIN Habitaciones ON Incidencias.id_habitacion = Habitaciones.id_habitacion;

6. Ver qué inquilino reporta cada incidencia
SELECT Inquilinos.nombre_cliente, Incidencias.descripcion
FROM Reportan
INNER JOIN Inquilinos ON Reportan.id_cliente = Inquilinos.id_cliente
INNER JOIN Incidencias ON Reportan.id_incidencia = Incidencias.id_incidencia;

7. Ver todos los inquilinos aunque no tengan incidencias (LEFT JOIN)
SELECT Inquilinos.nombre_cliente, Incidencias.descripcion
FROM Inquilinos
LEFT JOIN Reportan ON Inquilinos.id_cliente = Reportan.id_cliente
LEFT JOIN Incidencias ON Reportan.id_incidencia = Incidencias.id_incidencia;

8. Ver contratos y sus pagos
SELECT Contratos.id_contrato, Pagos.cantidad, Pagos.fecha_pago
FROM Contratos
LEFT JOIN Pagos ON Contratos.id_contrato = Pagos.id_contrato;

9. Ver número de pagos por contrato
SELECT id_contrato, COUNT(*) AS total_pagos
FROM Pagos
GROUP BY id_contrato;

10. Ver total pagado por cada inquilino
SELECT Inquilinos.nombre_cliente, SUM(Pagos.cantidad) AS total_pagado
FROM Pagos
INNER JOIN Contratos ON Pagos.id_contrato = Contratos.id_contrato
INNER JOIN Inquilinos ON Contratos.id_cliente = Inquilinos.id_cliente
GROUP BY Inquilinos.nombre_cliente;