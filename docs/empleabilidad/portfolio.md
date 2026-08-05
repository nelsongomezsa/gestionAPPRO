# Portfolio — Nelson Gomez Sanchez

Tengo 29 años y estoy estudiando 1º DAM en Prometeo by The Power. No es mi primera etapa laboral, así que sé lo que quiero y por qué estoy aquí: quiero cambiar de sector y dedicarme al desarrollo de software, con el foco puesto en banca y fintech. Esto es lo que he construido hasta ahora.

---

## Proyecto: GestionAp

**Repositorio:** [github.com/nelsongomezsa/gestion-alquiler-habitaciones](https://github.com/nelsongomezsa/gestion-alquiler-habitaciones)

### Por qué lo hice

Este es el proyecto intermodular de 1º DAM, así que tenía que cubrir todos los módulos del año. Pero quería que tuviese sentido real, no ser un ejercicio de laboratorio. Elegí la gestión de alquileres porque es un problema concreto: hay propietarios de pisos compartidos que llevan sus inquilinos y sus pagos en un Excel o directamente en papel, y eso acaba generando errores, impagos sin registrar y contratos que nadie controla.

Desde mi perspectiva, también era la excusa perfecta para construir algo que se pareciera a lo que me interesa profesionalmente: un sistema que gestiona datos financieros, controla operaciones y tiene que ser fiable.

### Qué hace

GestionAp es una aplicación de escritorio para gestionar el alquiler de habitaciones en pisos compartidos. Desde la app puedes:

- Ver qué habitaciones están libres, alquiladas o en mantenimiento
- Registrar inquilinos y asociarlos a sus habitaciones con un contrato
- Controlar los pagos mensuales y saber quién debe y cuánto
- Registrar y hacer seguimiento de incidencias
- Exportar los contratos a XML

No es una app enorme, pero está completa: tiene base de datos real, interfaz gráfica y funciona de principio a fin.

### Capturas de la app

Pantalla principal con el menú de navegación:

![Pantalla principal](../sistemas/capturas_app/Captura%20de%20pantalla%202026-04-23%20a%20las%202.18.20%20p.%20m..png)

Vista de gestión de habitaciones:

![Gestión de habitaciones](../sistemas/capturas_app/Captura%20de%20pantalla%202026-04-23%20a%20las%202.18.31%20p.%20m..png)

Vista de gestión de inquilinos y contratos:

![Gestión de inquilinos](../sistemas/capturas_app/Captura%20de%20pantalla%202026-04-23%20a%20las%202.18.36%20p.%20m..png)

Vista de pagos e incidencias:

![Pagos e incidencias](../sistemas/capturas_app/Captura%20de%20pantalla%202026-04-23%20a%20las%202.18.47%20p.%20m..png)

### Tecnologías que usé

| Tecnología | Para qué |
|---|---|
| Java 17 | Lenguaje principal de toda la aplicación |
| MySQL 8 | Base de datos donde se guarda todo |
| JDBC puro | Conexión Java–MySQL, sin ORM |
| JavaFX | Interfaz gráfica |
| XML + XSD | Exportación y validación de contratos |
| Maven | Gestión de dependencias y compilación |
| Git + GitHub | Control de versiones |

El código está organizado en capas: modelo, DAO, servicio y controlador. No es perfecta, pero tiene una estructura coherente y es fácil de navegar.

---

## Qué sé hacer ahora mismo

Siendo directo sobre lo que controlo y lo que todavía tengo que trabajar:

**Java**
Programación orientada a objetos: clases, herencia, interfaces, colecciones, manejo de excepciones. He construido una aplicación completa con él. Lo que me queda por profundizar: concurrencia, streams avanzados, el ecosistema de frameworks más allá de lo básico.

**SQL / MySQL**
Diseño de bases de datos relacionales desde cero: modelo entidad-relación, normalización, claves primarias y foráneas. Consultas con joins entre varias tablas, inserciones, actualizaciones y borrados. He trabajado con restricciones de integridad referencial en un proyecto real con seis tablas relacionadas.

**JDBC**
Conexión Java–MySQL sin ORM: apertura de conexiones, sentencias preparadas, procesado de resultados. Entiendo la diferencia entre Statement y PreparedStatement y por qué importa para evitar inyección SQL.

**JavaFX**
Interfaces gráficas con FXML y controladores, paso de datos entre pantallas, respuesta a eventos. Me ha costado, pero lo he sacado adelante y entiendo cómo funciona el ciclo de vida de la interfaz.

**XML y XSD**
Generación de XML desde código Java y validación contra un esquema XSD. Lo implementé en el módulo de exportación de contratos de GestionAp.

**Git**
Uso Git de forma disciplinada: commits con mensajes descriptivos, ramas, repositorio limpio sin credenciales ni ficheros locales. Entiendo para qué sirve `.gitignore` y lo aplico.

**Maven**
Configuración de proyectos, gestión de dependencias y ejecución con plugins. He resuelto problemas de configuración de JavaFX con Maven en distintos sistemas operativos.

---

## Qué he aprendido este año

Venía con nociones muy básicas de programación. Estas son las cosas que más me han cambiado la forma de pensar:

**Que organizar el código importa tanto como hacerlo funcionar.** Cuando el proyecto empieza a crecer, la estructura lo es todo. Aprender a separar capas —modelo, acceso a datos, lógica de negocio, interfaz— ha sido el cambio más importante en cómo programo.

**Que los datos tienen que ser consistentes desde el diseño.** Trabajar con claves foráneas e integridad referencial me ha enseñado que un modelo de datos mal pensado genera errores que luego son muy difíciles de corregir. Hay que pensar la estructura antes de escribir una línea de código.

**Que Git es comunicación, no solo almacenamiento.** Un historial de commits bien escrito cuenta la historia de cómo evolucionó el proyecto. Eso tiene valor cuando trabajas con otras personas o cuando vuelves al código después de semanas.

**Que los casos de error no son opcionales.** Una parte importante del trabajo es pensar qué pasa cuando algo falla: la base de datos no responde, el usuario introduce datos incorrectos, una operación se interrumpe a mitad. Eso no lo sabía gestionar bien al principio y lo he tenido que ir incorporando.

---

## A dónde quiero llegar

Quiero trabajar como desarrollador backend en banca o fintech. No es una elección al azar: me interesan los sistemas donde el software es crítico, donde los datos tienen que ser consistentes y auditables, y donde un fallo tiene consecuencias reales. Eso me exige un estándar alto, y eso me parece bien.

Tengo claro el camino a corto plazo: acabar 2º de DAM, aprender Spring Boot de forma autónoma en paralelo, construir algún proyecto propio orientado al sector (una API REST de pagos, un sistema con base de datos transaccional) y conseguir unas prácticas en un banco o fintech al terminar el ciclo.

BBVA Next Technologies y Santander Digital tienen programas para perfiles junior. Eso es lo que tengo en el punto de mira.

---

**Nelson Gomez Sanchez** · 1º DAM · Prometeo by The Power · nelsongomezsa@gmail.com
