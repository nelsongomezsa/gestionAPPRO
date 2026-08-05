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

### Validar con el programa Java

La clase `XmlValidator` (en `src/main/java/com/gestionap/utils/`) usa `javax.xml.validation`, disponible en el JDK estándar sin dependencias adicionales.

```bash
# Compilar (desde la raíz del proyecto)
mvn compile

# Validar el XML correcto
mvn exec:java -Dexec.mainClass="com.gestionap.utils.XmlValidator"

# Validar pasando rutas explícitas
mvn exec:java -Dexec.mainClass="com.gestionap.utils.XmlValidator" \
    -Dexec.args="xml/contratos.xml xml/esquema.xsd"

# Probar el XML inválido (debe imprimir INVÁLIDO)
mvn exec:java -Dexec.mainClass="com.gestionap.utils.XmlValidator" \
    -Dexec.args="xml/contrato_invalido.xml xml/esquema.xsd"
```

Salida esperada con `contratos.xml`:
```
=== Validador XML — GestionAp ===
XML : .../xml/contratos.xml
XSD : .../xml/esquema.xsd

RESULTADO: El XML es VÁLIDO. Cumple todas las restricciones del esquema.
```

Salida esperada con `contrato_invalido.xml`:
```
=== Validador XML — GestionAp ===
XML : .../xml/contrato_invalido.xml
XSD : .../xml/esquema.xsd

RESULTADO: El XML es INVÁLIDO.
Causa     : cvc-enumeration-valid: Value 'ocupada' is not facet-valid ...
```

### Validar con xmllint (alternativa de línea de comandos)

```bash
xmllint --schema xml/esquema.xsd xml/contratos.xml --noout
xmllint --schema xml/esquema.xsd xml/contrato_invalido.xml --noout
```

---

## Cómo encaja con el proyecto

GestionAp es una aplicación JavaFX + MySQL para gestionar el alquiler de habitaciones en pisos compartidos.

El módulo XML aporta:

1. **Exportación de datos**: la clase `XmlExporter` (ya existente en `utils/`) genera XML a partir de contratos cargados desde la base de datos. El formato de `contratos.xml` extiende esa idea con la jerarquía completa (inquilino, habitación, piso anidados).

2. **Interoperabilidad**: el XML permite intercambiar datos del sistema con otras aplicaciones o servicios externos sin necesidad de acceso directo a MySQL.

3. **Validación de integridad**: `XmlValidator` garantiza que cualquier XML que llegue al sistema (importación, integración) cumple las reglas de negocio antes de procesarlo (precios ≥ 100 €, DNI con formato legal, estados permitidos, etc.).

4. **Módulo de Lenguajes de Marcas**: este conjunto de ficheros cubre los contenidos curriculares de XML/XSD — estructura jerárquica, tipos de datos, patrones, enumeraciones, cardinalidades y validación programática desde Java.
