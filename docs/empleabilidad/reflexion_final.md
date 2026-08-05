# Reflexión Final — Nelson Gomez Sanchez

## Lo que aprendí haciendo GestionAp

Lo primero que aprendí es que hacer que algo funcione de verdad es bastante diferente a entender la teoría. Podía explicarte qué era una base de datos relacional antes de empezar este proyecto. Pero hasta que no me puse a conectar Java con MySQL de verdad, a gestionar la conexión, a hacer que una consulta devolviera datos y esos datos aparecieran en una tabla de JavaFX, no lo había entendido del todo.

Diseñar la base de datos fue de las partes que más me enseñó. Pensar en qué tablas necesitaba, qué columnas, cómo se relacionaban entre sí. Al principio parecía sencillo, pero en cuanto empecé a implementarlo me di cuenta de que había decisiones que luego afectaban a todo lo demás. Si una relación estaba mal planteada, te dabas cuenta al cabo de varios días cuando ya tenías código encima.

Conectar Java con MySQL usando JDBC también fue un aprendizaje nuevo para mí. Entender cómo se abre una conexión, cómo se lanza una consulta, cómo se recorre el ResultSet y se convierte en objetos que pueda usar en la interfaz. Eso lo vi muchas veces en apuntes pero hacerlo en un proyecto real donde los datos son tuyos y tienes que que funcione es otra cosa.

Y luego la parte de JavaFX. Hacer que la interfaz mostrara datos reales de la base de datos, que un TableView se llenara con lo que había en MySQL, fue de las cosas que más tiempo me costó pero también de las que más satisfacción me dio cuando funcionó.

## Qué me gustó más

La programación en Java, claramente. Es lo que más me gusta de todo el ciclo. Me gusta ver la lógica, entender cómo fluye el código de un sitio a otro, cómo una clase usa a otra, cómo un método devuelve algo que luego se muestra en pantalla.

Pero el momento que más recuerdo fue cuando la aplicación arrancó por primera vez y los datos de la base de datos aparecieron en la interfaz. Había habitaciones reales, inquilinos reales, contratos. Datos que yo había metido en MySQL y que ahora estaban en la pantalla de JavaFX a través del código que había escrito. Eso fue la primera vez en el proyecto que sentí que no estaba haciendo un ejercicio, sino algo que funcionaba de verdad.

También me gustó bastante el momento en que entendí cómo separar las responsabilidades: que el DAO no mezclara lógica de negocio, que el controlador de JavaFX no hiciera consultas directas a la base de datos. Cuando eso empezó a tener sentido en mi cabeza y el código empezó a verse más limpio, noté que estaba aprendiendo algo que va más allá de este proyecto concreto.

## Qué me costó más

El diagrama entidad-relación al principio. Me costó bastante entender bien las cardinalidades. Cuándo es una relación uno a muchos, cuándo es muchos a muchos, qué pasa cuando hay una relación ternaria. Lo leí varias veces, hice varios borradores del diagrama que tuve que tirar y volver a empezar porque al implementarlo veía que no tenía sentido.

Las relaciones entre tablas en general me costaron. Entender que una clave foránea no es solo un número, sino que representa un vínculo entre dos entidades y que si eso está mal planteado el modelo no refleja bien la realidad. Eso requirió tiempo y varios errores antes de que encajara.

También me costó la configuración de JavaFX con Maven. Hubo un momento en el que la app no arrancaba y no sabía si era un problema de dependencias, de la versión de Java, del módulo de JavaFX o de algo en el código. Ese tipo de errores donde no sabes ni por dónde empezar a buscar son los más frustrantes.

## Qué mejoraría de GestionAp

Bastantes cosas, siendo honesto.

Lo más evidente es el diseño visual. La interfaz funciona pero es bastante básica. Con más tiempo le metería más cariño al aspecto, colores más coherentes, mejor distribución de los elementos, que pareciera más una aplicación real y menos un ejercicio de clase.

Añadiría un login real. Ahora mismo no hay autenticación. Cualquiera que abra la app tiene acceso a todo. En una aplicación de verdad eso no puede ser así, habría que gestionar usuarios, contraseñas y permisos.

Más funcionalidades también. El módulo de pagos está, pero se podría ampliar mucho: historial de pagos, alertas de contratos a punto de vencer, informes. Hay muchas cosas que en un sistema real serían imprescindibles y que en esta versión están sin hacer o están a medias.

Y tests. No tengo ninguna prueba unitaria. Si tuviera que continuar este proyecto en serio, los tests serían lo primero que añadiría.

## Qué quiero hacer a partir de aquí

Tengo claro que quiero seguir mejorando en Java. Es el lenguaje con el que me siento más cómodo y creo que hay mucho más por aprender: colecciones, concurrencia, streams, patrones de diseño. Quiero profundizar en todo eso.

El siguiente paso concreto que me he puesto como objetivo es aprender Spring Boot. Es el framework que se usa en backend Java en casi todas partes, y especialmente en banca y fintech. Me parece el camino natural después de haber trabajado con JDBC y JavaFX.

Y a más largo plazo, quiero orientarme hacia el sector bancario como desarrollador. No como analista ni como gestor, sino como programador que construye sistemas. Me interesa ese mundo: los datos, la precisión, que las cosas tienen que funcionar bien porque están gestionando dinero de verdad. Creo que encaja con la forma en que me gusta trabajar.

---

Este proyecto no está terminado del todo ni es perfecto. Pero es el primero donde he sentido que estaba construyendo algo de principio a fin, tomando decisiones reales sobre cómo diseñarlo. Eso vale más que haber copiado algo que ya funcionaba.

---

*Nelson Gomez Sanchez · 1º DAM · Prometeo by The Power · Abril 2026*
