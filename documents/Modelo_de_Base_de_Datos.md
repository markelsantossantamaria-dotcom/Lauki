# 🗄️ Modelo de Base de Datos Relacional — Lauki

[⬅️ Volver al README principal](../README.md) · [🏗️ Ir a Arquitectura de Software](Arquitectura_de_Software.md)

---

## 1. Diccionario de las 8 Tablas y Estrategia JPA

Todas las tablas utilizan claves primarias enteras simples de 32 bits (`INT AUTO_INCREMENT` en SQL / `Integer id` en Java) y nombres de tabla en minúsculas para compatibilidad con contenedores Linux en Docker.

### 1.1. `CURSO` (`@Table(name = "curso")`)
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Identificador único del curso académico. |
| `nombre` | `VARCHAR(100)` | `String` | `UNIQUE NOT NULL` | Nombre de la promoción (ej. *"2º DAW 2026/27"*). |
| `activo` | `BOOLEAN` | `Boolean` | `NOT NULL DEFAULT TRUE` | Permite archivar promociones pasadas sin perder su historial. |

### 1.2. `USUARIO` (`@Table(name = "usuario")`)
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Identificador único del usuario. |
| `id_curso` | `INT` | `Curso` (`@ManyToOne`) | `NULL, FOREIGN KEY (curso.id)` | Nulo para `ADMINISTRADOR`; obligatorio para `ESTUDIANTE`. |
| `nombre` | `VARCHAR(100)` | `String` | `NOT NULL` | Nombre completo del profesor o estudiante. |
| `correo` | `VARCHAR(150)` | `String` | `UNIQUE NOT NULL` | Correo institucional usado como credencial en `/login`. |
| `contrasena` | `VARCHAR(255)` | `String` | `NOT NULL` | Hash cifrado con `BCryptPasswordEncoder`. |
| `rol_sistema` | `VARCHAR(20)` | `RolSistema` (Enum) | `NOT NULL` | `ADMINISTRADOR` o `ESTUDIANTE`. |
| `activo` | `BOOLEAN` | `Boolean` | `NOT NULL DEFAULT TRUE` | Si es `false`, bloquea el login (`isEnabled()`), manteniendo visible su autoría histórica. |

### 1.3. `RETO` (`@Table(name = "reto")`)
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Identificador único del reto. |
| `id_curso` | `INT` | `Curso` (`@ManyToOne`) | `NOT NULL, FOREIGN KEY (curso.id)` | Curso académico en el que se imparte el reto. |
| `nombre` | `VARCHAR(150)` | `String` | `NOT NULL` | Título del proyecto. |
| `descripcion` | `TEXT` | `String` | `NOT NULL` | Enunciado y objetivos del reto. |
| `estado_reto` | `VARCHAR(20)` | `EstadoReto` (Enum) | `NOT NULL` | `ACTIVO` (editable), `INACTIVO` (en preparación), `FINALIZADO` (histórico en solo lectura). |

### 1.4. `GRUPO` (`@Table(name = "grupo")`) — *Actúa como Equipo y Tablero Kanban*
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Identificador único del grupo/tablero. |
| `id_reto` | `INT` | `Reto` (`@ManyToOne`) | `NOT NULL, FOREIGN KEY (reto.id)` | Reto al que pertenece este equipo y su tablero Kanban. |
| `nombre` | `VARCHAR(100)` | `String` | `NOT NULL` | Nombre del equipo (ej. *"Equipo Alpha"*). |
| `fecha_creacion` | `TIMESTAMP` | `LocalDateTime` | `NOT NULL DEFAULT CURRENT_TIMESTAMP` | Fecha de alta del equipo. |

### 1.5. `MIEMBRO_GRUPO` (`@Table(name = "miembro_grupo")`) — *Miembros Actuales e Historial*
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Clave primaria simple. |
| `id_grupo` | `INT` | `Grupo` (`@ManyToOne`) | `NOT NULL, FOREIGN KEY (grupo.id)` | Grupo/tablero al que pertenece el alumno en ese reto. |
| `id_usuario` | `INT` | `Usuario` (`@ManyToOne`) | `NOT NULL, FOREIGN KEY (usuario.id)` | Estudiante integrante del equipo. |
| `rol_en_grupo` | `VARCHAR(20)` | `RolEnGrupo` (Enum) | `NOT NULL` | Etiqueta visual ETHAZI: `GESTOR`, `ANALISTA`, `DESARROLLADOR`, `COLABORADOR`. |
*Restricción adicional: `@UniqueConstraint(columnNames = {"id_grupo", "id_usuario"})`.*

### 1.6. `FASE` (`@Table(name = "fase")`)
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Identificador único de la fase. |
| `id_grupo` | `INT` | `Grupo` (`@ManyToOne`) | `NOT NULL, FOREIGN KEY (grupo.id)` | Grupo/tablero propietario de la fase. |
| `nombre_fase` | `VARCHAR(100)` | `String` | `NOT NULL` | Nombre de la etapa (se crean 3 por defecto al crear el grupo). |
| `orden` | `INT` | `Integer` | `NOT NULL` | Secuencia visual en el tablero (1, 2, 3...). |

### 1.7. `TAREA` (`@Table(name = "tarea")`)
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Identificador único de la tarea. |
| `id_fase` | `INT` | `Fase` (`@ManyToOne EAGER`) | `NOT NULL, FOREIGN KEY (fase.id)` | Fase a la que pertenece la tarea. |
| `id_asignado` | `INT` | `Usuario` (`@ManyToOne EAGER`)| `NULL, FOREIGN KEY (usuario.id)` | Responsable individual (obligatorio para salir de `PENDIENTE`). |
| `titulo` | `VARCHAR(200)` | `String` | `NOT NULL` | Título descriptivo de la tarea. |
| `descripcion` | `TEXT` | `String` | `NULL` | Detalle técnico del entregable. |
| `estado` | `VARCHAR(25)` | `EstadoTarea` (Enum) | `NOT NULL` | `PENDIENTE`, `EN_PROGRESO`, `BLOQUEADA`, `EN_SUPERVISION`, `COMPLETADA`. |
| `fecha_creacion_real` | `TIMESTAMP` | `LocalDateTime` | `NOT NULL` | Momento exacto de creación. |
| `fecha_inicializacion` | `TIMESTAMP` | `LocalDateTime` | `NULL` | Timestamp al salir de `PENDIENTE` por primera vez. |
| `fecha_esperada_finalizacion`| `DATE` | `LocalDate` | `NULL` | Fecha límite estimada (`<input type="date">`). |
| `fecha_finalizacion_real` | `TIMESTAMP` | `LocalDateTime` | `NULL` | Timestamp al llegar a `COMPLETADA` (`null` si se reabre). |

### 1.8. `LOG_TAREA` (`@Table(name = "log_tarea")`) — *Auditoría Inmutable*
| Columna | Tipo SQL | Tipo Java (JPA) | Restricciones | Propósito |
| :--- | :--- | :--- | :--- | :--- |
| `id` | `INT AUTO_INCREMENT` | `Integer` | `PRIMARY KEY` | Identificador secuencial del evento. |
| `id_tarea` | `INT` | `Tarea` (`@ManyToOne`) | `NOT NULL, FOREIGN KEY (tarea.id)` | Tarea auditada (`CascadeType.ALL` al borrar una tarea errónea). |
| `id_usuario` | `INT` | `Usuario` (`@ManyToOne EAGER`)| `NOT NULL, FOREIGN KEY (usuario.id)` | Autor del movimiento, bloqueo o comentario. |
| `tipo_evento` | `VARCHAR(20)` | `TipoEventoLog` (Enum) | `NOT NULL` | `CREACION`, `TRANSICION`, `BLOQUEO`, `COMENTARIO`. |
| `estado_anterior` | `VARCHAR(25)` | `EstadoTarea` (Enum) | `NULL` | Columna previa en cambios de estado. |
| `estado_nuevo` | `VARCHAR(25)` | `EstadoTarea` (Enum) | `NULL` | Nueva columna alcanzada. |
| `comentario` | `TEXT` | `String` | `NULL` | Texto obligatorio en `BLOQUEO` (motivo) y en `COMENTARIO`. |
| `fecha_registro` | `TIMESTAMP` | `LocalDateTime` | `NOT NULL` | Momento exacto del registro. |

---

## 2. Justificación de las Relaciones y Cumplimiento de 3FN

1. **Por qué `USUARIO` y `RETO` apuntan a `CURSO` (`N:1`):** Un curso escolar agrupa a muchos alumnos y varios retos a lo largo del año. Los administradores tienen `id_curso = NULL` para gestionar cualquier promoción.
2. **Por qué `GRUPO` apunta a `RETO` (`N:1`) y no a `CURSO`:** En ETHAZI los equipos rotan en cada reto. Al crear los grupos para cada reto concreto (`GRUPO.id_reto`), el grupo ya conoce su curso a través del reto (evitando redundancia en **Tercera Forma Normal - 3FN**), actúa como el propio Tablero Kanban del equipo y conserva en `MIEMBRO_GRUPO` quién participó en cada reto cuando el reto pasa a `FINALIZADO`.
3. **Por qué `FASE` apunta a `GRUPO` (`1:N`) y nunca se mezclan las tareas entre equipos del mismo reto:** Aunque todos los equipos de clase trabajen en el mismo `id_reto`, cada equipo tiene su propio `GRUPO.id` único (`10`, `11`, `12`...), y tanto `FASE` como `TAREA` cuelgan de `id_grupo`, garantizando aislamiento total entre tableros.
4. **Por qué `TAREA` tiene relación `N:1` con `USUARIO` (`id_asignado`):** Satisface la restricción de **responsabilidad individual** de cada unidad de trabajo.
