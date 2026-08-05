# Investigación del Sector — Banca y Tecnología Financiera

## Empresas del sector

### 1. BBVA — BBVA Next Technologies (España)

**Qué hace**
BBVA es uno de los mayores bancos del mundo por capitalización, y tiene una apuesta tecnológica muy seria. Su división **BBVA Next Technologies** es la rama de ingeniería del banco: un equipo de desarrollo interno que construye los sistemas y productos digitales de BBVA, desde la app móvil hasta la infraestructura de pagos y los sistemas de análisis de riesgo.

Es interesante porque no es una consultora que trabaja para el banco: es el banco mismo, desarrollando su propio software. Eso significa que los ingenieros que trabajan ahí tienen un impacto directo en productos que usan millones de personas.

**Qué perfiles contratan**
Buscan desarrolladores Java y Kotlin para backend, ingenieros de datos, especialistas en APIs, arquitectos de microservicios y perfiles con conocimiento de sistemas distribuidos. También tienen plazas para junior developers y programas de prácticas.

**Tecnologías que usan**
- Java y Kotlin como lenguajes principales de backend
- Spring Boot y Spring Cloud para microservicios
- Kafka para mensajería y eventos
- Kubernetes y OpenShift para orquestación de contenedores
- Bases de datos relacionales (Oracle, PostgreSQL) y NoSQL (MongoDB)
- APIs REST y GraphQL

---

### 2. Santander — Santander Digital (España / Internacional)

**Qué hace**
Santander es otro de los grandes bancos globales con sede en España, y **Santander Digital** es su área de transformación tecnológica. Tienen centros tecnológicos en España (principalmente Madrid y Boadilla del Monte) donde desarrollan soluciones digitales para sus mercados en Europa y América Latina.

Una de sus apuestas más conocidas es **Openbank**, el banco 100% digital del grupo Santander, construido desde cero sobre arquitectura cloud y microservicios. Es un buen ejemplo de cómo un banco tradicional puede construir tecnología moderna.

**Qué perfiles contratan**
Ingenieros Java backend, desarrolladores fullstack, especialistas en ciberseguridad, ingenieros de plataforma y data engineers. Tienen programas de prácticas para estudiantes de ciclos y grados técnicos, lo que lo hace accesible desde 2º de DAM.

**Tecnologías que usan**
- Java con Spring Boot como stack principal de backend
- AWS y Azure para infraestructura cloud
- Docker y Kubernetes
- Bases de datos relacionales y sistemas de mensajería
- Seguridad: OAuth2, JWT, certificados digitales

---

### 3. Bizum (España — Infraestructura de pagos)

**Qué hace**
Bizum no es exactamente una fintech independiente, pero merece un lugar aquí porque es uno de los sistemas de pagos más usados en España y es tecnología construida por un consorcio de bancos españoles. Técnicamente es gestionado por **Iberpay**, la sociedad española de sistemas de pago, y conecta en tiempo real los sistemas de más de 30 bancos.

Lo que hace Bizum interesante desde el punto de vista técnico es la complejidad que hay debajo: procesamiento de transacciones en tiempo real, garantías de consistencia entre múltiples sistemas bancarios, seguridad, auditoría. Es exactamente el tipo de sistema donde se aplica lo más exigente del desarrollo backend.

**Qué perfiles contratan**
Al ser un consorcio, los perfiles técnicos suelen entrar a través de los bancos participantes o de empresas tecnológicas asociadas. Buscan ingenieros con experiencia en sistemas de alta disponibilidad, protocolos de pago (ISO 20022, SEPA), seguridad y Java backend.

**Tecnologías que usan**
- Java como lenguaje principal
- Protocolos bancarios estándar: ISO 20022, SEPA, SWIFT
- Sistemas de mensajería de alta disponibilidad
- Bases de datos relacionales con transacciones ACID estrictas

---

## Perfiles profesionales de referencia

### Perfil 1: Desarrollador Java backend en un banco grande

Este tipo de perfil trabaja en el equipo de desarrollo interno de un banco (como BBVA Next Technologies o Santander Digital). Suele tener entre 3 y 7 años de experiencia y su día a día gira en torno a construir y mantener microservicios que procesan operaciones financieras: transferencias, consultas de saldo, generación de extractos, integraciones con sistemas externos.

En LinkedIn destacan habilidades como Java, Spring Boot, microservicios, bases de datos relacionales, APIs REST y conocimiento de normativa bancaria (PSD2, GDPR). En GitHub suelen tener proyectos propios donde demuestran que saben estructurar código, hacer tests y trabajar con patrones de diseño sólidos.

Lo que más me llevo de este perfil: que el camino empieza con Java bien aprendido, y que los bancos valoran mucho la solidez técnica frente a seguir las tendencias del momento. No necesitas conocer el framework más nuevo, necesitas entender bien los fundamentos.

---

### Perfil 2: Ingeniero en una fintech española (junior/mid)

Perfil diferente: alguien que trabaja en una fintech más pequeña, donde el equipo es reducido y cada persona toca más partes del sistema. Suelen tener entre 1 y 4 años de experiencia, muchos vienen de ciclos formativos o bootcamps, y se han especializado rápido porque la fintech no podía permitirse tenerles mucho tiempo en modo aprendizaje.

En LinkedIn destacan: Java o Python backend, APIs REST, integración con sistemas de pago, Agile/Scrum y a veces algo de cloud (AWS o GCP). En GitHub tienen proyectos donde se ve que entienden cómo conectar un backend con servicios externos, manejar errores de red y gestionar datos de forma segura.

Lo que más me llevo de este perfil: que en una fintech pequeña se aprende muy rápido, y que el conocimiento de sistemas de pago y APIs de terceros es muy valorado. También que entrar junior es posible si tienes proyectos concretos que demuestren que puedes construir algo real.

---

## Conclusiones

Después de investigar el sector me queda claro que:

1. **Java es el lenguaje del sector bancario** y no va a desaparecer: lleva décadas ahí y la industria ha construido demasiado encima de él para cambiarlo de golpe.
2. **Spring Boot es el framework estándar** para backend en banca y fintech española. Aprenderlo bien después del ciclo es la prioridad más obvia.
3. **Las bases de datos relacionales importan mucho.** En banca los datos tienen que ser consistentes, auditables y seguros. SQL sólido y entender transacciones ACID no es opcional.
4. **Los bancos grandes tienen programas de prácticas** accesibles desde DAM. BBVA y Santander contratan perfiles junior si tienen proyecto real y dominio de Java.
5. **La seguridad no es opcional.** En cualquier sistema financiero hay que pensar en autenticación, autorización, cifrado y auditoría desde el principio.

---

*Investigación realizada en abril de 2026 para el módulo 1709 de 1º DAM.*
