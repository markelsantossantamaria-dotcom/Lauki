# 📖 Especificación de Historias de Usuario (Product Backlog): "Mapeando el Caos" (Lauki)

> **Proyecto:** Reto 03 — Mapeando el Caos (**Lauki**)  
> **Centro:** Zabalburu Ikastetxea — 2º DAW (Metodología ETHAZI)  
> **Estándar:** Metodologías Ágiles / Product Backlog con Criterios de Aceptación BDD (*Behavior-Driven Development*)  
> **Tecnologías:** Spring Boot 3 + Spring Data JPA + Thymeleaf + Bootstrap 5 + JS Vanilla

---

## Matriz de Cobertura: Historias de Usuario vs. Casos de Uso

| Historia de Usuario | Título | Caso de Uso Vinculado | Rol Principal |
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

## Bloque 1: Seguridad y Acceso Unificado

### `[US-01]` — Autenticación Unificada y Redirección por Rol
- **Enunciado:** Como usuario (Administrador precargado o Estudiante matriculado), quiero iniciar sesión en `/login` con mi correo y contraseña, para acceder de forma segura a mi espacio de trabajo.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Login de Administrador y redirección al panel de gestión**
    - **Dado que** `DataInitializer.java` ha asegurado la existencia de mi cuenta `ADMINISTRADOR` en la tabla `USUARIO` con `activo = true`.
    - **Cuando** introduzco mi correo y contraseña en `/login` y pulso "Iniciar sesión".
    - **Entonces** el sistema valida mis credenciales y me redirige automáticamente al panel de administración (`/admin/cursos-usuarios`).
  - **Escenario 2: Login de Estudiante y redirección a su tablero**
    - **Dado que** el Administrador ha registrado mi usuario `ESTUDIANTE` en la base de datos con `activo = true`.
    - **Cuando** inicio sesión en `/login`.
    - **Entonces** el sistema me redirige automáticamente al tablero Kanban de mi reto activo (`/tablero`).
  - **Escenario 3: Credenciales erróneas o usuario inactivo**
    - **Dado que** introduzco una contraseña incorrecta o mi cuenta tiene `activo = false`.
    - **Cuando** intento iniciar sesión.
    - **Entonces** el sistema deniega el acceso y muestra el aviso *"Credenciales incorrectas o cuenta desactivada"*.

### `[US-02]` — Cierre de Sesión Seguro
- **Enunciado:** Como usuario autenticado, quiero cerrar mi sesión en cualquier momento, para evitar accesos no autorizados en ordenadores compartidos del aula.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Desconexión voluntaria**
    - **Dado que** tengo una sesión activa en la plataforma.
    - **Cuando** pulso el botón "Cerrar sesión".
    - **Entonces** el sistema destruye mi sesión HTTP y me redirige a `/login?logout=true`.

---

## Bloque 2: Administración Multi-Curso y Carga de Datos

### `[US-03]` — Alta de Cursos Académicos y Carga de Usuarios en Base de Datos
- **Enunciado:** Como Administrador autenticado, quiero dar de alta cursos escolares (ej. *"2º DAW 2026/27"*) y registrar en la base de datos a los estudiantes de cada promoción, para que la aplicación sea reutilizable año tras año.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Creación de curso y alta de estudiante con contraseña BCrypt**
    - **Dado que** he iniciado sesión como `ADMINISTRADOR`.
    - **Cuando** registro un nuevo `CURSO` y doy de alta a un alumno en `USUARIO` asignándole dicho curso y su contraseña inicial.
    - **Entonces** el servicio cifra la contraseña con `BCryptPasswordEncoder`, guarda al estudiante con `activo = true` y este ya puede iniciar sesión en `/login`.
  - **Escenario 2: Prevención de duplicados**
    - **Dado que** intento crear un curso o un correo de usuario ya existente.
    - **Cuando** envío el formulario.
    - **Entonces** el sistema impide el duplicado y muestra un mensaje informativo en pantalla.

### `[US-04]` — Creación de Retos y Grupos/Tableros con Fases Iniciales
- **Enunciado:** Como Administrador, quiero crear los retos de un curso y conformar los grupos de alumnos, para que cada equipo disponga inmediatamente de su tablero Kanban listo para trabajar.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Creación de grupo de reto con 3 fases automáticas**
    - **Dado que** existe un `RETO` en estado `ACTIVO` para un curso.
    - **Cuando** creo un `GRUPO` vinculado a dicho `RETO` y asigno a sus alumnos en `MIEMBRO_GRUPO`.
    - **Entonces** el sistema guarda el grupo, genera automáticamente 3 fases iniciales en `FASE` (*1. Análisis y Diseño*, *2. Desarrollo*, *3. Pruebas y Despliegue*) y vincula a los estudiantes con rol inicial `COLABORADOR`.

### `[US-05]` — Archivo de Cursos y Desactivación de Usuarios (`activo = false`)
- **Enunciado:** Como Administrador, quiero poder desactivar promociones finalizadas o alumnos que causen baja (`activo = false`), para mantener despejada la interfaz sin comprometer la persistencia histórica.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Desactivación limpia y preservación histórica**
    - **Dado que** selecciono un curso o un alumno.
    - **Cuando** cambio su estado a `activo = false`.
    - **Entonces** el alumno ya no puede iniciar sesión ni aparece para nuevos grupos, pero todos los tableros, tareas y comentarios históricos en los que intervino preservan su nombre íntegro.

---

## Bloque 3: Organización Interna del Equipo

### `[US-06]` — Autoasignación de Roles Internos ETHAZI (Etiqueta Visual)
- **Enunciado:** Como Estudiante, quiero seleccionar mi rol organizativo dentro de mi equipo (`GESTOR`, `ANALISTA`, `DESARROLLADOR`, `COLABORADOR`), para visibilizar mi responsabilidad técnica ante el equipo docente sin perder capacidades operativas sobre el tablero.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Actualización de rol organizativo**
    - **Dado que** pertenezco a un grupo de un reto activo y me sitúo en "Mi Equipo".
    - **Cuando** selecciono mi rol en el desplegable y guardo.
    - **Entonces** se actualiza `MIEMBRO_GRUPO.rol_en_grupo` y se muestra el badge correspondiente en la vista del equipo y en el histórico del reto.

### `[US-07]` — Estructuración y Ordenación de Fases del Tablero
- **Enunciado:** Como Estudiante, quiero añadir nuevas fases a las 3 generadas por defecto o suprimir fases vacías, para adaptar el tablero a la metodología de nuestro reto.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Alta de fase secuencial**
    - **Dado que** mi grupo tiene su reto en estado `ACTIVO`.
    - **Cuando** introduzco un `nombre_fase` y un `orden` ($\ge 1$).
    - **Entonces** la fase queda asociada a nuestro `id_grupo` y se integra en los filtros y columnas del tablero.
  - **Escenario 2: Restricción de borrado para fases con tareas**
    - **Dado que** intento eliminar una fase con tareas vinculadas (`existsByFaseId`).
    - **Cuando** confirmo la acción.
    - **Entonces** el sistema deniega el borrado indicando la existencia de tareas dependientes; si la fase está vacía, se elimina limpiamente.

---

## Bloque 4: Core Kanban, Auditoría e Historial

### `[US-08]` — Creación y Eliminación de Tareas del Tablero
- **Enunciado:** Como Estudiante, quiero registrar tareas vinculadas a una fase y descartar aquellas creadas por error, para mantener organizada la entrega del equipo.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Alta de tarea con trazabilidad inicial**
    - **Dado que** me encuentro en el tablero activo de mi equipo.
    - **Cuando** creo una tarea con `titulo`, `id_fase` y datos opcionales.
    - **Entonces** la tarjeta se ubica en `PENDIENTE`, muestra el badge de fase y genera un evento `CREACION` en `LOG_TAREA`.
  - **Escenario 2: Eliminación en cascada de tarea descartada**
    - **Dado que** una tarea fue generada por error.
    - **Cuando** solicito su eliminación.
    - **Entonces** el sistema elimina la entidad junto con sus registros en `LOG_TAREA` (`CascadeType.ALL`) sin violaciones de integridad referencial.

### `[US-09]` — Asignación de Responsable Individual de Tarea
- **Enunciado:** Como Estudiante, quiero asignar un único titular individual por tarea (`id_asignado`), para garantizar la propiedad y el seguimiento del trabajo según las directivas del reto.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Fijación del titular de la tarea**
    - **Dado que** consulto el detalle de una tarea en el tablero activo.
    - **Cuando** selecciono a un compañero de equipo como responsable (`id_asignado`) y confirmo.
    - **Entonces** la tarjeta refleja visiblemente el nombre del responsable individual asignado.

### `[US-10]` — Movimiento de Tarjetas Kanban y Registro Automático de Tiempos
- **Enunciado:** Como Estudiante, quiero desplazar las tareas entre las 5 columnas del tablero (`PENDIENTE`, `EN_PROGRESO`, `BLOQUEADA`, `EN_SUPERVISION`, `COMPLETADA`), para reflejar el progreso y registrar métricas temporales precisas.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Transición válida con responsable asignado**
    - **Dado que** una tarea cuenta con responsable individual (`id_asignado IS NOT NULL`).
    - **Cuando** la muevo fuera de `PENDIENTE`.
    - **Entonces** se fija `fecha_inicializacion` (si era nula) y se emite un evento `TRANSICION` en `LOG_TAREA`.
  - **Escenario 2: Denegación de inicio sin responsable**
    - **Dado que** una tarea en `PENDIENTE` carece de responsable asignado (`id_asignado IS NULL`).
    - **Cuando** intento desplazarla a otra columna.
    - **Entonces** el backend responde con error `400`, la interfaz alerta de la obligatoriedad de designar un responsable y la tarjeta permanece en `PENDIENTE`.
  - **Escenario 3: Finalización en `COMPLETADA`**
    - **Dado que** una tarea alcanza la columna `COMPLETADA`.
    - **Cuando** se suelta la tarjeta.
    - **Entonces** el sistema fija `fecha_finalizacion_real = CURRENT_TIMESTAMP`.

### `[US-11]` — Bloqueo Justificado, Desbloqueo y Comentarios de Seguimiento
- **Enunciado:** Como Estudiante, quiero bloquear una tarea aportando obligatoriamente el motivo e incorporar notas de evolución, para visibilizar obstáculos y acuerdos técnicos.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Bloqueo justificado atómico**
    - **Dado que** sitúo una tarea con responsable en la columna `BLOQUEADA`.
    - **Cuando** aporto el motivo en el modal obligatorio y guardo.
    - **Entonces** la tarea adopta el estado `BLOQUEADA` y se persiste un evento `BLOQUEO` con el motivo en `LOG_TAREA`.
  - **Escenario 2: Incorporación de comentarios**
    - **Dado que** abro la vista detallada de una tarea.
    - **Cuando** añado una nota técnica.
    - **Entonces** se anota un evento `COMENTARIO` en `LOG_TAREA` accesible en la cronología de la tarea.

### `[US-12]` — Cierre Académico de Reto
- **Enunciado:** Como Administrador, quiero transicionar un reto a `FINALIZADO`, para congelar los tableros y habilitar la rotación de equipos para el siguiente ciclo.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Finalización e inmutabilidad histórica**
    - **Dado que** un reto se encuentra en estado `ACTIVO`.
    - **Cuando** el Administrador pulsa "Finalizar Reto".
    - **Entonces** el reto pasa a `FINALIZADO`, inmovilizando todos los grupos asociados en modo solo lectura para histórico y permitiendo crear el siguiente reto.

### `[US-13]` — Consulta de Proyectos Finalizados en Modo Solo Lectura
- **Enunciado:** Como Estudiante, quiero examinar tableros de retos anteriores en los que participé, para consultar el trabajo realizado y la composición de los equipos que integré.
- **Criterios de Aceptación (BDD):**
  - **Escenario 1: Navegación de solo lectura**
    - **Dado que** formé parte de un equipo cuyo reto está `FINALIZADO`.
    - **Cuando** accedo a su visualización desde el historial.
    - **Entonces** el tablero se presenta completo (fases, tareas, métricas, autores) en modo inmutable sin controles de arrastre ni edición.
