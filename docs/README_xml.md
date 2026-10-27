# Módulo XML — GestionAp

## Qué datos representa el XML

`contratos.xml` contiene los contratos de alquiler activos del sistema. Cada contrato agrupa en un único documento toda la información necesaria para entender una relación de alquiler:

| Bloque | Campos |
|--------|--------|
| Contrato | `id`, `fechaInicio`, `fechaFin`, `precioMensual` |
| Inquilino | `id`, `nombre`, `apellidos`, `dni`, `telefono`, `email` |
| Habitación | `id`, `numero`, `precio`, `estado` |
| Piso | `id`, `direccion`, `ciudad` |

Los datos reproducen fielmente las filas de la base de datos MySQL (tablas `Contratos`, `Inquilinos`, `Habitaciones`, `Pisos`, `Ciudades` definidas en `sql/datos.sql`).

---

## Estructura de ficheros

```
xml/
├── contratos.xml          # Datos válidos — 6 contratos reales
├── esquema.xsd            # Esquema XSD que define y valida la estructura
└── contrato_invalido.xml  # Ejemplo inválido para demostrar que el XSD funciona
```

---

## Cómo se valida

### Restricciones del esquema (`esquema.xsd`)

| Campo | Tipo XSD | Restricción |
|-------|----------|-------------|
| `fechaInicio`, `fechaFin` | `xs:date` | formato `YYYY-MM-DD` |
| `precioMensual`, `precio` | `xs:decimal` | `minInclusive = 100` |
| `dni` | `xs:string` | patrón `[0-9]{8}[A-Z]` |
| `email` | `xs:string` | patrón `[^@\s]+@[^@\s]+\.[^@\s]+` |
| `estado` | enumeración | `disponible` \| `alquilada` \| `mantenimiento` |
| `id` (atributos) | `xs:integer` | requerido en contrato, inquilino, habitacion, piso |
| número de contratos | — | `minOccurs="1"`, `maxOccurs="unbounded"` |

### Validar con xmllint

Las clases Java `XmlExporter` y `XmlValidator` vivían en la versión inicial del proyecto ([GestionAP](https://github.com/nelsongomezsa/GestionAP)). En GestionAp PRO se eliminaron en el commit `c4c9933` porque la aplicación no las usaba. La validación se hace desde la línea de comandos:

```bash
xmllint --schema xml/esquema.xsd xml/contratos.xml --noout
xmllint --schema xml/esquema.xsd xml/contrato_invalido.xml --noout
```

---

## Cómo encaja con el proyecto

GestionAp es una aplicación JavaFX + MySQL para gestionar el alquiler de habitaciones en pisos compartidos.

El módulo XML aporta:

1. **Modelo de datos portable**: `contratos.xml` representa los contratos con su jerarquía completa (inquilino, habitación y piso anidados), con los mismos datos que la base MySQL.

2. **Interoperabilidad**: el XML permite intercambiar datos del sistema con otras aplicaciones sin acceso directo a MySQL.

3. **Reglas de negocio en el esquema**: `esquema.xsd` define lo que debe cumplir cualquier XML que llegue al sistema (precios ≥ 100 €, DNI con formato legal, estados permitidos, etc.).

4. **Módulo de Lenguajes de Marcas**: estructura jerárquica, tipos de datos, patrones, enumeraciones, cardinalidades y validación contra XSD.
