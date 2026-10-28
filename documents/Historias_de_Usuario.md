# 📖 Product Backlog e Historias de Usuario (User Stories) — Lauki

[⬅️ Volver al README principal](../README.md) · [📋 Ir a Casos de Uso](Casos_de_Uso.md)

---

## Matriz de Cobertura del Product Backlog

| ID | Título de la Historia de Usuario | Caso de Uso | Rol Principal |
| :---: | :--- | :---: | :---: |
| **`[US-01]`** | Autenticación Unificada y Redirección por Rol | `CU-01` | Estudiante / Administrador |
| **`[US-02]`** | Cierre de Sesión Seguro | `CU-06` | Estudiante / Administrador |
| **`[US-03]`** | Alta de Cursos Académicos y Carga de Usuarios en Base de Datos | `CU-02` | Administrador |
| **`[US-04]`** | Creación de Retos y Grupos/Tableros con Fases Iniciales | `CU-02` | Administrador |
| **`[US-05]`** | Archivo de Cursos y Desactivación de Usuarios (`activo = false`) | `CU-02` | Administrador |
| **`[US-06]`** | Autoasignación de Roles Internos ETHAZI (Etiqueta Visual) | `CU-03` | Estudiante |
| **`[US-07]`** | Estructuración y Ordenación de Fases del Tablero | `CU-04` | Estudiante |
| **`[US-08]`** | Creación y Eliminación de Tareas del Tablero | `CU-05` | Estudiante |
| **`[US-09]`** | Asignación de Responsable Individual de Tarea | `CU-05` | Estudiante |
| **`[US-10]`** | Movimiento de Tarjetas Kanban y Registro Automático de Tiempos | `CU-05` | Estudiante |
| **`[US-11]`** | Bloqueo Justificado, Desbloqueo y Comentarios de Seguimiento | `CU-05` | Estudiante |
| **`[US-12]`** | Cierre Académico de Reto | `CU-08` | Administrador |
| **`[US-13]`** | Consulta de Proyectos Finalizados en Modo Solo Lectura | `CU-07` | Estudiante |

---

## Especificación BDD (Dado / Cuando / Entonces)

### Bloque 1: Seguridad y Acceso
- **`[US-01]` Autenticación Unificada y Redirección por Rol:**
  - *Enunciado:* Como usuario (Administrador precargado o Estudiante), quiero iniciar sesión en `/login` con mi correo y contraseña para acceder a mi panel.
  - *Escenario 1 (Login exitoso):* **Dado que** mi usuario tiene `activo = true`, **cuando** introduzco mis credenciales válidas en `/login`, **entonces** el sistema me redirige a `/admin/cursos-usuarios` (si soy `ADMINISTRADOR`) o a `/tablero` (si soy `ESTUDIANTE`).
  - *Escenario 2 (Acceso denegado):* **Dado que** las credenciales son erróneas o `activo == false`, **cuando** envío el formulario, **entonces** muestra un mensaje de error genérico sin iniciar sesión.
- **`[US-02]` Cierre de Sesión Seguro:**
  - *Enunciado:* Como usuario autenticado, quiero cerrar mi sesión para proteger mi cuenta en equipos compartidos.
  - *Escenario 1:* **Dado que** tengo sesión activa, **cuando** pulso "Cerrar sesión", **entonces** se invalida la sesión HTTP y redirige a `/login?logout=true`.

---

### Bloque 2: Administración Multi-Curso
- **`[US-03]` Alta de Cursos Académicos y Carga de Usuarios en Base de Datos:**
  - *Enunciado:* Como Administrador, quiero crear cursos escolares y dar de alta a los estudiantes en la base de datos para utilizar la plataforma en promociones sucesivas.
  - *Escenario 1:* **Dado que** estoy en `/admin/cursos-usuarios`, **cuando** registro un `CURSO` y un `USUARIO` estudiante, **entonces** su contraseña se almacena cifrada con BCrypt y queda habilitado con `activo = true`.
- **`[US-04]` Creación de Retos y Grupos/Tableros con Fases Iniciales:**
  - *Enunciado:* Como Administrador, quiero crear retos y conformar los grupos de cada reto para que cada equipo tenga su tablero Kanban.
  - *Escenario 1:* **Dado que** existe un reto `ACTIVO`, **cuando** creo un `GRUPO` asociado a él y asigno a sus alumnos en `MIEMBRO_GRUPO`, **entonces** se generan automáticamente 3 fases iniciales para ese grupo.
- **`[US-05]` Archivo de Cursos y Desactivación de Usuarios:**
  - *Enunciado:* Como Administrador, quiero desactivar cursos antiguos o usuarios que causen baja (`activo = false`) sin romper el historial.
  - *Escenario 1:* **Dado que** marco `activo = false` en un usuario, **cuando** se guarda el cambio, **entonces** ya no puede iniciar sesión pero sus tareas y comentarios antiguos conservan su nombre.

---

### Bloque 3: Organización del Equipo
- **`[US-06]` Autoasignación de Roles Internos ETHAZI:**
  - *Enunciado:* Como Estudiante, quiero seleccionar mi rol visual (`GESTOR`, `ANALISTA`, `DESARROLLADOR`, `COLABORADOR`) para organizar el equipo sin limitar mis permisos.
  - *Escenario 1:* **Dado que** estoy en `/grupo/equipo`, **cuando** cambio mi rol y guardo, **entonces** se actualiza `MIEMBRO_GRUPO.rol_en_grupo` y se muestra el badge correspondiente.
- **`[US-07]` Estructuración y Ordenación de Fases del Tablero:**
  - *Enunciado:* Como Estudiante, quiero añadir nuevas fases o borrar las vacías para adaptar el tablero a nuestro proyecto.
  - *Escenario 1:* **Dado que** solicito borrar una fase en `/grupo/fases`, **cuando** el sistema verifica si tiene tareas (`existsByFaseId`), **entonces** impide el borrado si contiene tareas o la elimina si está vacía.

---

### Bloque 4: Core Kanban, Auditoría e Historial
- **`[US-08]` Creación y Eliminación de Tareas:**
  - *Enunciado:* Como Estudiante, quiero crear tareas asociadas a una fase y descartar las erróneas.
  - *Escenario 1:* **Dado que** creo una tarea en el tablero activo, **cuando** se guarda, **entonces** nace en estado `PENDIENTE` y registra un evento `CREACION` en `LOG_TAREA`.
- **`[US-09]` Asignación de Responsable Individual:**
  - *Enunciado:* Como Estudiante, quiero asignar a un miembro del equipo como responsable titular (`id_asignado`) de cada tarea.
  - *Escenario 1:* **Dado que** selecciono un responsable del grupo en el modal de la tarea, **cuando** guardo, **entonces** su nombre aparece en la tarjeta.
- **`[US-10]` Movimiento de Tarjetas Kanban y Registro Automático de Tiempos:**
  - *Enunciado:* Como Estudiante, quiero arrastrar las tarjetas por las 5 columnas para reflejar el progreso y medir tiempos.
  - *Escenario 1 (Sin responsable):* **Dado que** una tarea en `PENDIENTE` tiene `id_asignado == null`, **cuando** intento moverla a otra columna, **entonces** el sistema rechaza el movimiento (`HTTP 400`) y pide asignar responsable.
  - *Escenario 2 (Con responsable):* **Dado que** tiene responsable asignado, **cuando** sale de `PENDIENTE` o llega a `COMPLETADA`, **entonces** captura `fecha_inicializacion` / `fecha_finalizacion_real` y registra `TRANSICION` en `LOG_TAREA`.
- **`[US-11]` Bloqueo Justificado y Comentarios de Seguimiento:**
  - *Enunciado:* Como Estudiante, quiero justificar obligatoriamente cualquier bloqueo y dejar comentarios en las tareas.
  - *Escenario 1:* **Dado que** muevo una tarea a `BLOQUEADA`, **cuando** envío el motivo en el modal obligatorio, **entonces** se actualiza el estado y se inserta el evento `BLOQUEO` con el motivo en `LOG_TAREA`.
- **`[US-12]` Cierre Académico de Reto:**
  - *Enunciado:* Como Administrador, quiero finalizar un reto para congelar sus tableros en modo solo lectura.
  - *Escenario 1:* **Dado que** pulso "Finalizar Reto", **cuando** pasa a `FINALIZADO`, **entonces** todos los grupos de ese reto quedan bloqueados frente a ediciones.
- **`[US-13]` Consulta de Proyectos Finalizados en Modo Solo Lectura:**
  - *Enunciado:* Como Estudiante, quiero revisar mis tableros de retos pasados aunque hoy esté en otro grupo.
  - *Escenario 1:* **Dado que** accedo a `/tablero/historial/{idGrupo}`, **cuando** se carga la vista, **entonces** veo todas las fases, tareas, comentarios y compañeros de aquel reto en modo solo lectura.
