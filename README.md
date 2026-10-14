# Reto 03: Mapeando el Caos (LAUKI)

Herramienta web colaborativa para la gestión ágil de proyectos educativos bajo el modelo **ETHAZI** en **Zabalburu Ikastetxea** (Ciclo Formativo de Grado Superior en Desarrollo de Aplicaciones Web - 2º DAW).

---

## 1. Visión del Producto
En el modelo pedagógico ETHAZI, el alumnado trabaja en retos multidisciplinares donde la autoorganización del equipo, la toma compartida de decisiones y el reparto equitativo de tareas son determinantes. Con frecuencia surgen problemáticas como:
- Dificultades para planificar las tareas y falta de claridad en la titularidad o responsabilidades individuales.
- Dificultad para conocer el estado y progreso real del trabajo frente al planificado.
- Ausencia de anticipación y visibilidad temprana de bloqueos o impedimentos técnicos.

**LAUKI** resuelve esta problemática mediante un prototipo web ligero, usable e interactivo que permite descomponer retos en fases estructuradas y tareas operativas dentro de un tablero Kanban con trazabilidad estricta y control de tiempos de ejecución real.

---

## 2. Puntos Clave de la Arquitectura
- **Filosofía Zero-Budget:** Arquitectura orientada a coste cero de infraestructura, eliminando dependencias de servicios cloud de pago o plataformas SaaS propietarias.
- **Monolito Modular:** Aplicación autocontenida desarrollada con Java, Spring Boot y Spring MVC, estructurada internamente por dominios para minimizar la complejidad operativa sin comprometer la mantenibilidad.
- **Renderizado del Lado del Servidor (SSR):** Vistas generadas dinámicamente en servidor mediante Thymeleaf y maquetadas con Bootstrap y CSS3.
- **Interactividad Ligera (Vanilla JS):** Tablero visual gestionado mediante JavaScript puro (sin frameworks pesados de frontend como React o Angular), permitiendo el movimiento de tarjetas y la interacción dinámica con el backend.
- **Autogestión de Roles Internos:** Los propios alumnos se autoasignan los roles organizativos dentro de su equipo (Gestor, Analista, Desarrollador, Colaborador) mediante la entidad intermedia `Miembro_Grupo`.
- **Auditoría y Trazabilidad Unificada (`LOG_TAREA`):** Registro inmutable en una única tabla de auditoría (`LOG_TAREA`) que consolida transiciones de estado, justificación obligatoria de bloqueos y comentarios en transacciones atómicas.
- **Borrado Lógico Centralizado:** Implementación a nivel de persistencia en JPA/Hibernate (`@SQLDelete` y `@SQLRestriction` sobre `fecha_eliminacion`) que mantiene limpios los controladores y servicios sin borrar físicamente registros ni alterar auditorías.
- **Auditoría Temporal Inmutable:** Control estricto de marcas de tiempo en el servidor (`fecha_creacion_real` inmutable al crearse; `fecha_inicializacion` persistente al pasar por primera vez a *En Progreso*; `fecha_finalizacion_real` calculada al entrar a *Completada* y reseteable en caso de reapertura).

---

## 3. Stack Tecnológico Obligatorio

| Componente | Tecnología | Propósito |
| :--- | :--- | :--- |
| **Backend** | Spring Boot / Spring MVC (Java) | Controlador principal, inyección de dependencias, lógica de negocio y APIs |
| **Persistencia** | Spring Data JPA / Hibernate | Mapeo objeto-relacional, auditoría y borrado lógico centralizado |
| **Motor de Plantillas** | Thymeleaf | Generación de vistas HTML dinámicas del lado del servidor |
| **Diseño y Maquetación** | HTML5, CSS3 + Bootstrap 5 | Interfaz responsiva, accesible y componentes de validación visual |
| **Lógica Frontend** | JavaScript Vanilla | Tablero Kanban interactivo, Drag and Drop y consumo asíncrono |
| **Base de Datos** | Relacional (PostgreSQL / MySQL) | Almacenamiento íntegro, integridad referencial y transacciones atómicas |
| **Entorno y Pruebas ASIR** | Docker & Docker Compose | Aislamiento y reproducibilidad para testeo por el ciclo ASIR |
| **Testing Automatizado** | JUnit & Spring Boot Test | Pruebas unitarias de reglas de dominio, criterios de aceptación y transacciones |
| **Control de Versiones** | Git | Trazabilidad del desarrollo, ramas y colaboración en equipo |

---

## 4. Índice de Documentación Técnica

La documentación detallada del sistema se encuentra estructurada en el directorio `documentation/`:

- **Documento de Requisitos de Software (SRS):**
  - **Funcionales:** Lo que la app debe hacer (ej. *"El usuario debe poder registrarse con Google"*, *"El sistema genera un PDF de facturación"*).
  - **No funcionales:** Rendimiento, seguridad y escalabilidad (ej. *"Tiempos de carga menores a 2 segundos"*, *"Soporte para 10,000 usuarios concurrentes"*).
- **Arquitectura de Software y Diagramas:**
  - **Diagrama de Arquitectura:** Estructura Frontend, Backend, Base de Datos y APIs de terceros.
  - **Modelo de Base de Datos (DER):** Esquema de tablas/colecciones y relaciones entre datos.
  - **Diagramas de Flujo (User Flow):** El camino que recorre el usuario para completar cada acción clave.
- **Diseño e Interfaz (UI/UX):**
  - **Wireframes y Prototipos:** Bocetos visuales (Figma, Sketch, Adobe XD) de las pantallas principales.
  - **Guía de Estilos:** Paleta de colores, tipografías y componentes de interfaz.
- **Documentación de APIs:** Especificación de endpoints (REST o GraphQL) con parámetros de entrada/salida (usando OpenAPI/Swagger).
- **Manual de Despliegue y DevOps:** Pasos para levantar el entorno de desarrollo, servidor de producción (AWS, Vercel, Docker) y configuración de CI/CD.

---

## 5. Validación y Entorno Educativo
El prototipo está diseñado para ejecutarse y desplegarse en un entorno local. Como parte fundamental de la metodología ETHAZI y la cooperación interciclos en **Zabalburu Ikastetxea**, la aplicación será sometida a pruebas funcionales, de usabilidad y de rendimiento por parte del alumnado del CFGS de **Administración de Sistemas Informáticos en Red (ASIR)**, validando su comportamiento en un entorno real de trabajo colaborativo.
