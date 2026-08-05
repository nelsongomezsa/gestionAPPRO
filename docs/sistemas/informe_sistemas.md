# Informe Técnico del Entorno de Ejecución — GestionAp
**Módulo: Sistemas Informáticos (0483) · 1º DAM**
**Autor: Nelson Gómez Sánchez**
**Fecha: 23 de abril de 2026**

---

## 1. Dónde se ejecuta la aplicación

GestionAp es una aplicación de escritorio. No necesita ningún servidor en internet ni nada en la nube. Se instala y se ejecuta directamente en el ordenador del propietario que la va a usar.

La idea es que el propietario abre la app en su PC, gestiona sus pisos, sus inquilinos y sus contratos, y los datos se guardan en una base de datos MySQL que también está en ese mismo ordenador (gracias a XAMPP). Todo local, sin conexión a ningún servidor externo.

---

## 2. Requisitos del equipo

Para que la aplicación funcione bien, el ordenador necesita cumplir unos requisitos mínimos. Estos son los que he calculado teniendo en cuenta que la app usa JavaFX para la interfaz gráfica y MySQL para guardar los datos.

### Mínimos (lo justo para que arranque)

| Componente | Mínimo |
|---|---|
| Procesador | Dual-core a 1,5 GHz |
| RAM | 2 GB |
| Espacio en disco | 500 MB libres |
| Resolución de pantalla | 1024 × 768 |

Con estos mínimos la app funciona, pero puede ir un poco lenta si el ordenador tiene muchas cosas abiertas a la vez.

### Recomendados (para que vaya fluido)

| Componente | Recomendado |
|---|---|
| Procesador | Dual-core a 2 GHz o más |
| RAM | 4 GB o más |
| Espacio en disco | 1 GB libre |
| Resolución de pantalla | 1280 × 800 o superior |

**Por qué esos números:**

- **RAM:** Java en general consume bastante memoria. La JVM sola ya ocupa entre 100 y 200 MB, JavaFX suma otro poco y MySQL también necesita su parte. Con 2 GB vas justo; con 4 GB vas tranquilo.
- **Disco:** El espacio se necesita principalmente para instalar Java (~300 MB), XAMPP (~500 MB) y Maven (~100 MB). Los datos de la base de datos en sí pesan muy poco.
- **Procesador:** JavaFX renderiza la interfaz gráfica y si el procesador es muy antiguo o lento, la app puede tardar en cargar las pantallas.

---

## 3. Sistema operativo

Yo he desarrollado GestionAp en macOS (en un Mac con chip Apple Silicon), pero la app funciona en Windows, Linux y macOS sin problema. La razón es Java.

Java es lo que se llama un lenguaje multiplataforma. Cuando compilas el código, no genera un ejecutable específico de Windows o de Mac, sino algo llamado bytecode. Ese bytecode lo ejecuta la JVM (Java Virtual Machine), que es un programa que existe para cada sistema operativo. Así que el código que yo he escrito en mi Mac lo puede ejecutar un Windows sin tocar nada del código fuente.

Lo único que hay que tener en cuenta es que en el `pom.xml` tengo configurado JavaFX con el clasificador `mac-aarch64` (para mi chip M1/M2). Si alguien lo va a usar en Windows o Linux, tiene que cambiar ese clasificador al de su sistema. Más adelante lo explico en la instalación.

---

## 4. Instalación paso a paso

Estos pasos sirven para instalar GestionAp desde cero en cualquier ordenador. Los sigo en orden.

### Paso 1 — Instalar Java 17 o superior

GestionAp necesita Java 17 como mínimo (así está configurado en el `pom.xml`).

1. Entrar en [https://adoptium.net](https://adoptium.net) y descargar el JDK 17 o superior para tu sistema operativo.
2. Instalar siguiendo el asistente.
3. Comprobar que ha quedado bien abierto una terminal y escribiendo:

```bash
java -version
```

Tiene que salir algo como `openjdk version "17.x.x"`.

### Paso 2 — Instalar XAMPP

XAMPP instala MySQL en el ordenador y el panel phpMyAdmin para gestionar la base de datos fácilmente.

1. Descargar XAMPP desde [https://www.apachefriends.org](https://www.apachefriends.org).
2. Instalar y abrir el panel de control de XAMPP.
3. Arrancar los módulos **Apache** y **MySQL**.

### Paso 3 — Instalar Maven

Maven es la herramienta que gestiona las dependencias del proyecto y lo ejecuta.

1. Descargar Maven desde [https://maven.apache.org/download.cgi](https://maven.apache.org/download.cgi).
2. Descomprimir y añadir la carpeta `bin` al PATH del sistema.
3. Comprobar con:

```bash
mvn -version
```

### Paso 4 — Clonar el repositorio

Abrir una terminal, ir a la carpeta donde quieras guardar el proyecto y ejecutar:

```bash
git clone https://github.com/nelsongomezsanchez/gestion-alquiler-habitaciones.git
cd gestion-alquiler-habitaciones
```

### Paso 5 — Crear la base de datos con phpMyAdmin

1. Abrir el navegador y entrar en `http://localhost/phpmyadmin`.
2. Crear una base de datos nueva llamada `gestion_alquileres`.
3. Con la base de datos seleccionada, ir a la pestaña **Importar**.
4. Importar primero el fichero `sql/base_datos.sql` (crea las tablas).
5. Importar después el fichero `sql/datos.sql` (mete datos de ejemplo).

### Paso 6 — Configurar database.properties

En la carpeta `src/main/resources/` hay un fichero llamado `database.properties.example`. Hay que copiarlo y renombrarlo a `database.properties`:

```bash
cp src/main/resources/database.properties.example src/main/resources/database.properties
```

Luego abrirlo y rellenar los datos de conexión:

```properties
db.url=jdbc:mysql://localhost:3306/gestion_alquileres?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
db.user=root
db.password=tu_contraseña_de_mysql
db.driver=com.mysql.cj.jdbc.Driver
```

Si XAMPP está recién instalado y no has puesto contraseña al root de MySQL, deja `db.password` en blanco.

### Paso 7 — Ajustar el pom.xml si no usas Mac Apple Silicon

Si estás en Windows, cambia las líneas del `pom.xml` que dicen `mac-aarch64` por el clasificador correcto:

- Windows: `win`
- Linux: `linux`
- Mac Intel: `mac`

### Paso 8 — Ejecutar la aplicación

Con XAMPP corriendo (Apache y MySQL activos), desde la raíz del proyecto ejecutar:

```bash
mvn javafx:run
```

Maven descargará las dependencias la primera vez (necesita internet) y luego arrancará la aplicación.

---

## 5. Usuarios y estructura de carpetas

### Quién usa la aplicación

GestionAp está pensada para **un único usuario**: el propietario de los pisos. No tiene sistema de roles ni multiusuario. Hay una pantalla de login al arrancar, pero es para proteger el acceso básico a la app, no para gestionar múltiples cuentas.

El propietario puede hacer todo: ver y editar inquilinos, habitaciones, contratos, pagos e incidencias.

### Dónde se guardan los datos

Todos los datos se guardan en la base de datos MySQL `gestion_alquileres` que corre en XAMPP de forma local. No hay ficheros de datos sueltos en el disco (salvo los XML de contratos que se pueden exportar desde la app, que van a la carpeta `xml/`).

### Estructura de carpetas del proyecto

```
gestion-alquiler-habitaciones/
├── src/
│   └── main/
│       ├── java/com/gestionap/
│       │   ├── controller/     → Controladores de cada pantalla JavaFX
│       │   ├── dao/            → Acceso a la base de datos (consultas SQL)
│       │   ├── model/          → Clases que representan los datos (Inquilino, Contrato…)
│       │   ├── service/        → Lógica de negocio entre el controlador y el DAO
│       │   ├── database/       → Conexión con MySQL
│       │   └── utils/          → Exportación e importación de XML
│       └── resources/
│           ├── *.fxml          → Pantallas de la interfaz gráfica
│           └── database.properties   → Configuración de la BD (no sube a git)
├── sql/
│   ├── base_datos.sql          → Script que crea todas las tablas
│   └── datos.sql               → Script con datos de ejemplo
├── xml/
│   ├── contratos.xml           → Exportación de contratos
│   └── esquema.xsd             → Validación del XML de contratos
├── docs/sistemas/
│   ├── informe_sistemas.md     → Este informe
│   └── capturas_app/           → Capturas de pantalla de la app funcionando
├── diagramas/
│   └── Gestionap.jpg           → Diagrama de la base de datos
└── pom.xml                     → Configuración de Maven (dependencias y plugins)
```

---

## 6. Mantenimiento

### Actualizaciones

De momento GestionAp no tiene sistema de actualizaciones automáticas. Para actualizar, el usuario tiene que descargar la nueva versión del repositorio con `git pull` y volver a ejecutar `mvn javafx:run`. No es complicado pero requiere que el usuario tenga git instalado.

### Copias de seguridad de la base de datos

Esto es lo más importante del mantenimiento. Si el disco del ordenador falla y no hay copia de seguridad, se pierden todos los datos de inquilinos, contratos y pagos.

La forma más sencilla de hacer una copia es exportar la base de datos desde phpMyAdmin:

1. Entrar en `http://localhost/phpmyadmin`.
2. Seleccionar la base de datos `gestion_alquileres`.
3. Ir a **Exportar** y guardar el fichero `.sql` en un lugar seguro (un disco externo o la nube).

Recomiendo hacerlo una vez a la semana como mínimo, o siempre antes de hacer cambios importantes.

También se puede hacer desde la terminal con:

```bash
mysqldump -u root -p gestion_alquileres > copia_backup_$(date +%Y%m%d).sql
```

### Qué hacer si algo falla

**La app no arranca:**
- Comprobar que XAMPP está abierto y que Apache y MySQL están en verde.
- Comprobar que el fichero `database.properties` existe y tiene los datos correctos.
- Revisar que Java 17+ está instalado con `java -version`.

**No conecta a la base de datos:**
- Verificar que MySQL está activo en XAMPP.
- Comprobar el usuario y la contraseña en `database.properties`.
- Asegurarse de que la base de datos `gestion_alquileres` existe en phpMyAdmin.

**Error al ejecutar `mvn javafx:run`:**
- Si es la primera vez, puede ser que Maven esté descargando dependencias. Esperar y volver a intentar.
- Si da error de plataforma (mac/win/linux), revisar el clasificador en el `pom.xml`.

---

## 7. Evidencias

Las capturas de pantalla de la aplicación funcionando están en la carpeta `docs/sistemas/capturas_app/`. Muestran las pantallas principales de la app en ejecución.

| Captura | Descripción |
|---|---|
| `Captura de pantalla 2026-04-23 a las 2.18.20 p. m..png` | Pantalla principal de la aplicación |
| `Captura de pantalla 2026-04-23 a las 2.18.31 p. m..png` | Vista de gestión |
| `Captura de pantalla 2026-04-23 a las 2.18.36 p. m..png` | Vista de gestión |
| `Captura de pantalla 2026-04-23 a las 2.18.47 p. m..png` | Vista de gestión |

> Las capturas están en la carpeta `docs/sistemas/capturas_app/` del repositorio.
