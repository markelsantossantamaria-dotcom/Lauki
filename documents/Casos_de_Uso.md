# 📋 Especificación de Casos de Uso (Use Cases) — Lauki

[⬅️ Volver al README principal](../README.md) · [📖 Ir a Historias de Usuario](Historias_de_Usuario.md)

---

## Índice de Casos de Uso

| Código | Nombre del Caso de Uso | Actores Principales | Módulo del Sistema |
| :---: | :--- | :--- | :--- |
| **CU-01** | Autenticación Unificada y Redirección por Rol | Estudiante, Administrador | Seguridad / Sesión |
| **CU-02** | Gestión Académica Multi-Curso (Cursos, Usuarios, Retos y Grupos) | Administrador | Administración Global |
| **CU-03** | Asignación de Roles Internos ETHAZI (Informativo) | Estudiante | Gestión de Equipos |
| **CU-04** | Estructuración del Tablero en Fases | Estudiante | Planificación de Reto |
| **CU-05** | Ciclo de Vida Kanban y Auditoría de Tareas | Estudiante | Core Kanban |
| **CU-06** | Cierre de Sesión Seguro | Estudiante, Administrador | Seguridad / Sesión |
| **CU-07** | Consulta de Historial de Retos Finalizados (Solo Lectura) | Estudiante | Histórico / Trazabilidad |
| **CU-08** | Cierre Académico de Reto | Administrador | Administración Global / Histórico |

---

## CU-01: Autenticación Unificada y Redirección por Rol
- **Descripción:** Permite tanto a los Administradores (precargados inicialmente por `DataInitializer.java`) como a los Estudiantes (dados de alta en la base de datos por el Administrador) iniciar sesión en `/login` y ser redirigidos automáticamente según su rol.
- **Precondiciones:** El usuario existe en `USUARIO` con `activo = true`.
- **Flujo Principal:**
  1. El usuario introduce `correo` y `contrasena` en `/login` y pulsa "Iniciar sesión".
  2. Spring Security verifica el usuario activo y su hash BCrypt.
  3. Redirige según `rol_sistema`:
     - `ADMINISTRADOR` $\rightarrow$ `/admin/cursos-usuarios`.
     - `ESTUDIANTE` $\rightarrow$ `/tablero`.
- **Flujo Alternativo (3a):** Si las credenciales fallan o `activo == false`, muestra *"Credenciales incorrectas o cuenta desactivada"*.

---

## CU-02: Gestión Académica Multi-Curso (Cursos, Usuarios, Retos y Grupos)
- **Descripción:** El Administrador prepara el curso escolar creando el curso, cargando a los alumnos en la base de datos y configurando los retos y grupos.
- **Flujo Principal:**
  1. **Alta de Curso:** Crea un `CURSO` con `nombre` único y `activo = true`.
  2. **Carga de Usuarios:** Registra a los alumnos en `USUARIO` cifrando su contraseña con `BCryptPasswordEncoder` y vinculándolos al `id_curso`.
  3. **Alta de Reto:** Crea un `RETO` vinculado al curso con `estado_reto = 'ACTIVO'`.
  4. **Alta de Grupo/Tablero:** Crea un `GRUPO` vinculado al `id_reto`. El servicio genera automáticamente **3 fases iniciales en `FASE`** (*1. Análisis y Diseño*, *2. Desarrollo*, *3. Pruebas y Despliegue*).
  5. **Asignación de Miembros:** Vincula a los estudiantes del curso en `MIEMBRO_GRUPO` con rol inicial `COLABORADOR`.
- **Flujos Alternativos:**
  - **1a/2a (Duplicados):** Impide registrar un correo o nombre de curso ya existente.
  - **6a (Archivo):** Permite marcar `activo = false` en cursos o usuarios que causen baja sin romper las claves foráneas históricas.

---

## CU-03: Asignación de Roles Internos ETHAZI (Informativo)
- **Descripción:** Los miembros del grupo eligen su rol organizativo (`GESTOR`, `ANALISTA`, `DESARROLLADOR`, `COLABORADOR`) como etiqueta visual informativa.
- **Flujo Principal:**
  1. El Estudiante accede a **Mi Equipo** (`/grupo/equipo`).
  2. Selecciona su rol en el desplegable y guarda.
  3. Se actualiza `MIEMBRO_GRUPO.rol_en_grupo` y se muestra el badge correspondiente.

---

## CU-04: Estructuración del Tablero en Fases
- **Descripción:** Permite añadir nuevas fases ordenadas a las 3 creadas por defecto o eliminar fases vacías.
- **Flujo Principal:**
  1. El Estudiante accede a **Fases del Reto** (`/grupo/fases`), introduce `nombre_fase` y `orden` ($\ge 1$) y guarda.
- **Flujo Alternativo (4a - Borrado Seguro):** Antes de eliminar una fase, comprueba `tareaRepository.existsByFaseId(idFase)`. Si tiene tareas, impide el borrado pidiendo moverlas o eliminarlas antes.

---

## CU-05: Ciclo de Vida Kanban y Auditoría de Tareas
- **Precondiciones:** El Estudiante pertenece al `GRUPO` dueño del tablero y su `RETO` está `ACTIVO`.
- **Subflujos Operativos:**
  - **A. Creación de Tarea (`POST /tablero/tareas`):** Guarda la tarea en `PENDIENTE` con `fecha_creacion_real` y registra `CREACION` en `LOG_TAREA`.
  - **B. Asignación de Responsable Individual (`POST /tablero/tareas/{id}/asignar`):** Asigna a un miembro del grupo como titular (`id_asignado`).
  - **C. Transición de Estado y Tiempos (`PATCH /api/tareas/{id}/estado`):**
    - Si `nuevoEstado != PENDIENTE` y `id_asignado == null`, rechaza con `HTTP 400`.
    - Al salir de `PENDIENTE` por primera vez, asigna `fecha_inicializacion`.
    - Al entrar en `COMPLETADA`, asigna `fecha_finalizacion_real` (y la limpia a `null` si se reabre).
    - Inserta evento `TRANSICION` en `LOG_TAREA`.
  - **D. Bloqueo Justificado (`POST /api/tareas/{id}/bloquear`):** Exige motivo no vacío, cambia el estado a `BLOQUEADA` e inserta evento `BLOQUEO` en `LOG_TAREA`.
  - **E. Comentarios (`POST /tablero/tareas/{id}/comentarios`):** Inserta evento `COMENTARIO` en `LOG_TAREA`.
  - **F. Eliminación (`POST /tablero/tareas/{id}/eliminar`):** Elimina una tarea creada por error junto con sus logs en cascada (`CascadeType.ALL`).

---

## CU-06: Cierre de Sesión Seguro
- **Flujo Principal:** El usuario pulsa "Cerrar sesión" (`POST /logout`), invalidando la sesión HTTP y redirigiendo a `/login?logout=true`.

---

## CU-07: Consulta de Historial de Retos Finalizados (Solo Lectura)
- **Flujo Principal:**
  1. El Estudiante consulta sus grupos cuyos retos tienen `estado_reto == FINALIZADO`.
  2. Selecciona un proyecto pasado (`/tablero/historial/{idGrupo}`).
  3. El sistema verifica su pertenencia en `MIEMBRO_GRUPO` y renderiza el tablero con `soloLectura = true`.

---

## CU-08: Cierre Académico de Reto
- **Flujo Principal:** El Administrador pulsa "Finalizar Reto" en `/admin/grupos-retos`. El sistema cambia `reto.setEstadoReto(EstadoReto.FINALIZADO)`, dejando automáticamente todos los grupos/tableros de ese reto en modo solo lectura.
