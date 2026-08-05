# GestionAp

Proyecto Intermodular de 1º DAM · Prometeo by The Power

Aplicación de escritorio para gestionar el alquiler de habitaciones en pisos compartidos, desarrollada en Java con conexión a base de datos MySQL.

---

## ¿Para qué sirve?

Muchos propietarios de pisos compartidos llevan sus alquileres con papel o Excel, lo que acaba generando líos: pagos que no cuadran, incidencias sin registrar, contratos que vencen sin avisar.

**GestionAp** resuelve eso, Con esta aplicación puedes:

- Ver de un vistazo qué habitaciones están libres, alquiladas o en mantenimiento
- Registrar inquilinos y asociarlos a sus habitaciones con un contrato
- Llevar el control de pagos mensuales y saber quién debe qué
- Guardar y hacer seguimiento de incidencias reportadas por los inquilinos
- Exportar los datos a XML para tener un registro estructurado

---

## Tecnologías

| Herramienta | Para qué se usa |
|---|---|
| Java 17 | Lenguaje principal de la aplicación |
| JavaFX | Interfaz gráfica de usuario |
| MySQL 8 | Base de datos local |
| JDBC puro | Conexión entre Java y MySQL, sin ORM |
| XML + XSD | Exportación de datos y validación |
| Maven 3.9+ | Gestión de dependencias y compilación |
| Git + GitHub | Control de versiones |

---

## Cómo ejecutarlo

### Requisitos previos

Necesitas tener instalado en tu máquina:
- Java 17 JDK (recomendado [Temurin de Adoptium](https://adoptium.net))
- Maven 3.9+
- MySQL 8 o MariaDB 10.6+

### Pasos

**1. Clona el repositorio**
```bash
git clone https://github.com/nelsongomezsa/gestion-alquiler-habitaciones.git
cd gestion-alquiler-habitaciones
```

**2. Crea la base de datos**
```bash
mysql -u root -p < sql/schema.sql
mysql -u root -p < sql/datos.sql
```

**3. Configura la conexión**

Crea el fichero `src/main/resources/database.properties` con tus datos:
```properties
db.url=jdbc:mysql://localhost:3306/gestion_alquileres
db.user=root
db.password=tu_contraseña
```
> Este fichero está en el `.gitignore` — nunca se sube al repositorio.

**4. Compila y arranca**
```bash
mvn clean package
mvn javafx:run
```

---

## Estructura del proyecto

```
gestion-alquiler-habitaciones/
├── src/
│   └── main/
│       ├── java/com/gestionap/
│       │   ├── Main.java           # Arranque de la aplicación
│       │   ├── model/              # Clases de dominio: Piso, Habitacion, Inquilino...
│       │   ├── dao/                # Acceso a la base de datos con JDBC
│       │   ├── service/            # Lógica de negocio
│       │   ├── controller/         # Controladores de la interfaz JavaFX
│       │   └── utils/              # Exportación XML y validación XSD
│       └── resources/
│           ├── fxml/               # Pantallas de la interfaz
│           ├── css/                # Estilos visuales
│           └── database.properties # Credenciales (NO subir — ver .gitignore)
├── sql/
│   ├── schema.sql                  # Creación de tablas
│   ├── datos.sql                   # Datos de prueba
│   └── consultas.sql               # Consultas del sistema
├── xml/
│   ├── datos.xml                   # Exportación de datos
│   └── esquema.xsd                 # Validación XML
├── diagrams/
│   └── er_diagram.png              # Diagrama Entidad–Relación
├── docs/
│   ├── README_bbdd.md              # Documentación base de datos (0484)
│   ├── README_xml.md               # Documentación XML/XSD (0373)
│   ├── sistemas/                   # Informe técnico del entorno (0483)
│   └── empleabilidad/              # Perfil profesional (1709)
├── .gitignore
├── pom.xml
└── README.md
```

---

## Qué cubre cada módulo

| Módulo | Qué aporta este proyecto |
|---|---|
| 0484 Bases de Datos | Diseño E/R, modelo relacional, scripts SQL y consultas reales |
| 0485 Programación | Aplicación Java funcional con CRUD completo vía JDBC |
| 0373 Lenguajes de Marcas | Exportación de contratos a XML validado con XSD |
| 0483 Sistemas Informáticos | Informe del entorno de ejecución e instalación |
| 0487 Entornos de Desarrollo | Repositorio GitHub organizado con historial de commits coherente |
| MPO Ampliación | Arquitectura en capas, diseño orientado a objetos, código limpio |

---

## Si algo falla

| Error | Causa probable | Solución |
|---|---|---|
| No conecta con la BD | Credenciales incorrectas | Revisa `database.properties` |
| `java` no reconocido | JDK no está en el PATH | Configura la variable de entorno |
| Error al compilar | Dependencias no descargadas | Ejecuta `mvn clean install` con internet |
| La ventana no abre | JavaFX no está cargado | Usa `mvn javafx:run`, no `java -jar` |

---

## Autor

**Nelson Gomez Sanchez** · 1º DAM · Prometeo by The Power

---

## Licencia

[MIT](https://opensource.org/licenses/MIT)
