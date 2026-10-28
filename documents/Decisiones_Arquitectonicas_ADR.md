# 🏛️ Registros de Decisiones Arquitectónicas (ADRs) — Lauki

[⬅️ Volver al README principal](../README.md) · [🏗️ Ir a Arquitectura de Software](Arquitectura_de_Software.md)

---

## 1. Catálogo de Decisiones Arquitectónicas (`ADR-01` a `ADR-05`)

### ADR-01: `DataInitializer` Exclusivo para Administradores y Gestión Multi-Curso en Web
- **Estado:** Aceptado.
- **Contexto:** La aplicación no permite auto-registro público de estudiantes, pero debe poder utilizarse en futuros cursos escolares sin modificar el código fuente.
- **Decisión:** `DataInitializer.java` comprueba al arrancar si existe la cuenta inicial de `ADMINISTRADOR` (`existsByCorreo`) y la inserta con contraseña cifrada en BCrypt. Tanto Administradores como Estudiantes inician sesión en `/login`. Desde el panel web (`/admin/cursos-usuarios`), el Administrador crea los cursos (`CURSO`) y carga en la base de datos a los estudiantes (`USUARIO`).
- **Consecuencias:** Garantiza el acceso docente desde el primer despliegue en Docker y permite reutilizar la plataforma año tras año.

---

### ADR-02: Unificación de Equipo, Tablero e Historial (`GRUPO.id_reto`)
- **Estado:** Aceptado.
- **Contexto:** En la metodología ETHAZI, los alumnos cambian de compañeros de grupo al pasar de un reto al siguiente, pero deben conservar acceso en modo solo lectura a los tableros de sus retos finalizados.
- **Decisión:** Vincular la tabla `GRUPO` directamente con `RETO` (`id_reto FK`) y usar clave primaria simple `Integer id` en `MIEMBRO_GRUPO` (`UNIQUE(id_grupo, id_usuario)`).
- **Consecuencias:**
  - Cada `GRUPO` actúa directamente como el Tablero Kanban de ese equipo en ese reto (con 3 fases iniciales creadas por defecto).
  - Cuando un reto pasa a `FINALIZADO`, el grupo y sus filas en `MIEMBRO_GRUPO` quedan congelados automáticamente como historial en solo lectura sin necesitar tablas adicionales ni procesos de copia.

---

### ADR-03: Estados de Dominio (`activo` booleano y `EstadoReto`) en lugar de `@SQLDelete`
- **Estado:** Aceptado.
- **Contexto:** El uso de `@SQLRestriction` global en Hibernate provoca errores `EntityNotFoundException` cuando un log histórico (`LOG_TAREA`) referencia a un usuario o tarea dada de baja lógica.
- **Decisión:** Utilizar un atributo estándar `boolean activo = true` en `CURSO` y `USUARIO` (integrado con `UserDetails.isEnabled()` de Spring Security), el enumerado `EstadoReto` (`ACTIVO`, `INACTIVO`, `FINALIZADO`) en `RETO`, y borrado en cascada (`CascadeType.ALL, orphanRemoval = true`) de `Tarea` hacia `LogTarea` solo cuando se elimina una tarea creada por equivocación.
- **Consecuencias:** Cero errores de claves foráneas en el historial y consultas Spring Data JPA limpias.

---

### ADR-04: Responsable Individual Único (`id_asignado`) en `TAREA`
- **Estado:** Aceptado.
- **Contexto:** El enunciado del reto exige que cada tarea tenga un responsable individual claramente identificado.
- **Decisión:** Modelar una relación `@ManyToOne` `id_asignado` en `TAREA` (nula al crearse en `PENDIENTE` y obligatoria para avanzar a cualquier otra columna), prescindiendo de tablas `@ManyToMany` de colaboradores secundarios.
- **Consecuencias:** Refuerza la titularidad individual en ETHAZI y simplifica el mapeo JPA.

---

### ADR-05: Conversión `Record.from(Entity)` en Java 21 e Interacción Híbrida en el Kanban
- **Estado:** Aceptado.
- **Contexto:** Reducir el código repetitivo y facilitar el mantenimiento entre los 4 integrantes del equipo.
- **Decisión:**
  1. Incluir métodos factoría estáticos `TareaDTO.from(Tarea)` y `LogTareaDTO.from(LogTarea)` dentro de los propios `record` de Java 21, eliminando clases Mapper intermedias.
  2. Limitar `kanban.js` (< 60 líneas) al Drag & Drop de estado, modal de bloqueo y filtro por fase, resolviendo la creación, asignación y comentarios mediante formularios `<form method="post">` de Thymeleaf.
- **Consecuencias:** Reduce el tamaño del proyecto a ~44 archivos y evita desincronizaciones del DOM en el navegador.

---

## 2. Guía de Prevención de Fallos Durante el Desarrollo

| # | Riesgo Técnico al Programar | Medida Aplicada en Lauki |
| :---: | :--- | :--- |
| **1** | Duplicar el Admin al reiniciar el servidor | Comprobar siempre `if (!usuarioRepository.existsByCorreo(...))` en `DataInitializer`. |
| **2** | Alumnos dados de alta que no pueden hacer login | Cifrar siempre con `passwordEncoder.encode()` en `AdminCursoUsuarioService`. |
| **3** | `LazyInitializationException` al renderizar tarjetas | Usar `FetchType.EAGER` en `fase` y `asignado` dentro de `Tarea.java` y `@Transactional` en los servicios. |
| **4** | Error de Foreign Key al borrar una tarea con logs | Declarar `cascade = CascadeType.ALL, orphanRemoval = true` en `Tarea.logs`. |
| **5** | Desincronización en JS si el backend rechaza un Drag & Drop | En `kanban.js`, verificar `if (!res.ok)` mostrando el error y ejecutar `window.location.reload()`. |
| **6** | Error `null` en Thymeleaf si el alumno aún no tiene reto activo | Envolver el tablero en `<div th:if="${grupo != null}">` con aviso informativo si es `null`. |
| **7** | Sensibilidad a mayúsculas de MySQL en el Docker Linux de ASIR | Definir todos los `@Table(name = "...")` en minúsculas (`curso`, `usuario`, `reto`, `grupo`, `fase`, `tarea`, `log_tarea`). |
| **8** | Conexión JDBC en Docker Compose | Parametrizar `spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3306/lauki}`. |
