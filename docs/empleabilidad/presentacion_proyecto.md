# Presentación del Proyecto — GestionAp

## Cómo lo presentaría yo

Si tuviese que explicar este proyecto en una entrevista en un banco o en una fintech, lo haría así:

---

## Qué es GestionAp

GestionAp es una aplicación de escritorio que desarrollé como proyecto intermodular de 1º DAM. Gestiona el alquiler de habitaciones en pisos compartidos: inquilinos, contratos, pagos e incidencias, todo conectado a una base de datos relacional real.

Sé que a primera vista puede parecer un proyecto de gestión inmobiliaria, pero cuando lo explico en un contexto bancario, me gusta hacer una lectura diferente: es un sistema que gestiona datos financieros (pagos, contratos, importes, fechas de vencimiento), garantiza la integridad de esa información y genera registros exportables y auditables. Eso, en esencia, es lo mismo que hace un sistema bancario, a otra escala.

## Qué problema resuelve

El problema concreto es que muchos propietarios de pisos compartidos llevan sus alquileres con papel o con Excel: pagos que no cuadran, contratos sin registrar, incidencias que se pierden. GestionAp centraliza todo eso y lo hace trazable.

Desde el punto de vista técnico, el problema que resuelve es de gestión de datos con coherencia: varios tipos de entidades relacionadas entre sí (pisos, habitaciones, inquilinos, contratos, pagos), operaciones que deben ejecutarse de forma íntegra o no ejecutarse, y un historial que tiene que ser fiable.

Ese tipo de problema es el núcleo de cualquier sistema financiero.

## Para quién está pensado

En este caso, para un propietario pequeño que quiere algo más organizado que una hoja de cálculo. Pero el patrón es el mismo que el de un sistema bancario: un actor (el propietario / el banco) que necesita gestionar relaciones contractuales con múltiples clientes, controlar flujos de pagos y tener trazabilidad de todo lo que ocurre.

## Tecnologías que usa

- **Java 17** — lenguaje principal, el mismo que domina el backend de la mayoría de bancos y fintechs españolas
- **MySQL 8 con JDBC puro** — conexión directa a base de datos sin ORM, lo que me ha obligado a entender bien SQL, transacciones y el ciclo de vida de las conexiones
- **JavaFX** — interfaz gráfica con la que he aprendido a separar la presentación de la lógica de negocio
- **XML + XSD** — exportación y validación de datos estructurados, un formato muy presente en los sistemas de intercambio bancario (SEPA, ISO 20022 usa XML)
- **Maven** — gestión del proyecto y sus dependencias
- **Git + GitHub** — control de versiones con historial limpio y documentación

La arquitectura sigue un patrón en capas: modelo de dominio, capa DAO para acceso a datos, capa de servicio para la lógica de negocio, y controladores para la interfaz. No es perfecta, pero es coherente con cómo se estructuran los sistemas backend profesionales.

## Conexión directa con el mundo bancario

Cuando pienso en qué tiene este proyecto que sea relevante para trabajar en banca o fintech, identifico cuatro cosas concretas:

**1. Gestión de datos financieros con integridad**
La tabla de Pagos tiene claves foráneas hacia Contratos, que a su vez apuntan a Habitaciones e Inquilinos. Cualquier operación que rompa esa cadena lanza un error de integridad referencial. Eso es exactamente lo que no puede permitirse un sistema bancario: datos huérfanos o transacciones a medias.

**2. Control de contratos y vencimientos**
Los contratos tienen fecha de inicio y fecha de fin. El sistema puede saber qué contratos están activos y cuáles han vencido. En banca, la gestión de contratos de crédito, hipotecas o productos financieros funciona con la misma lógica.

**3. Trazabilidad de pagos**
Cada pago queda registrado con fecha, importe, método y contrato asociado. En cualquier sistema financiero, la auditabilidad de las transacciones es un requisito legal, no opcional.

**4. Exportación estructurada de datos**
La exportación a XML con validación XSD es un mecanismo de intercambio de datos estandarizado. El formato XML es el que usan protocolos como SEPA o ISO 20022 para el intercambio entre entidades financieras.

## Qué he aprendido que es aplicable a banca

Más allá del código, lo que más me llevo de este proyecto es haber construido algo donde los datos importan de verdad. No es una app de prueba con datos inventados que puedes borrar sin consecuencias. Tiene una estructura donde cada dato tiene un significado y donde una operación mal hecha puede dejar el sistema en un estado incorrecto.

Aprender a pensar así —a pensar en la integridad, en la consistencia, en qué pasa si algo falla a mitad— es exactamente lo que necesito para trabajar en entornos donde los datos son dinero real.

---

*Nelson Gomez Sanchez · 1º DAM · Prometeo by The Power · Proyecto Intermodular 2025–2026*
