# 📋 Especificación de Casos de Uso del Sistema: "Mapeando el Caos" (Lauki)

> **Proyecto:** Reto 03 — Mapeando el Caos (**Lauki**)  
> **Centro:** Zabalburu Ikastetxea — 2º DAW (Metodología ETHAZI)  
> **Estándar:** IEEE 830 / Especificación de Casos de Uso  
> **Tecnologías:** Spring Boot 3 (Spring MVC) + Spring Data JPA (Hibernate 6) + Thymeleaf + Bootstrap 5 + JS Vanilla

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

### Descripción
Permite tanto a los Administradores (precargados inicialmente por `DataInitializer.java`) como a los Estudiantes (dados de alta en la base de datos por el Administrador) iniciar sesión en la misma pantalla (`/login`) y ser redirigidos automáticamente a su espacio de trabajo según su rol.

### Actores
- **Administrador (Equipo Docente):** Accede al panel de administración global para cargar cursos, usuarios, grupos y retos.
- **Estudiante:** Accede al tablero Kanban activo de su equipo y a su historial de retos.

### Precondiciones
1. Al arrancar la aplicación, `DataInitializer.java` ha verificado e insertado (si no existía) la cuenta inicial de `ADMINISTRADOR` en la tabla `USUARIO` con contraseña cifrada en BCrypt.
2. El usuario tiene `activo = true` en la tabla `USUARIO`.
3. El usuario se encuentra en la pantalla pública de Login (`/login`).

### Flujo Principal
1. El usuario introduce su `correo` y su `contrasena` en `/login`.
2. El usuario envía el formulario pulsando "Iniciar sesión".
3. Spring Security invoca a `CustomUserDetailsService`, busca el registro en `USUARIO` mediante `findByCorreo(correo)`, verifica que `activo == true` y valida el hash BCrypt de la contraseña.
4. El sistema identifica el `rol_sistema` (`ADMINISTRADOR` o `ESTUDIANTE`) y establece el contexto de seguridad.
5. El manejador de autenticación redirige según el rol:
   - Si es **`ADMINISTRADOR`** $\rightarrow$ Redirige al Panel de Administración (`/admin/cursos-usuarios`).
   - Si es **`ESTUDIANTE`** $\rightarrow$ Redirige al Tablero Kanban de su reto activo (`/tablero`).

### Flujos Alternativos
- **3a. Credenciales incorrectas o cuenta inactiva:** Si el correo no existe, `activo == false` o la contraseña no coincide, el sistema redirige a `/login?error=true` con el aviso *"Credenciales incorrectas o cuenta desactivada"*.

### Postcondiciones
- Sesión autenticada establecida y navegación hacia la vista correspondiente.

---

## CU-02: Gestión Académica Multi-Curso (Cursos, Usuarios, Retos y Grupos)

### Descripción
El Administrador prepara el entorno para las promociones escolares: da de alta cursos académicos, registra usuarios en base de datos, define retos y conforma los grupos de trabajo (cada grupo actúa como el Tablero Kanban del equipo y recibe automáticamente 3 fases iniciales).

### Actores
- **Administrador:** Docente autenticado con `rol_sistema = 'ADMINISTRADOR'`.

### Precondiciones
1. El Administrador ha iniciado sesión en `/login` con su cuenta precargada por `DataInitializer`.

### Flujo Principal
1. **Alta de Curso:** El Administrador accede a **Cursos y Usuarios** (`/admin/cursos-usuarios`) y crea un nuevo `CURSO` indicando su `nombre` único (ej. *"2º DAW 2026/27"*), con `activo = true`.
2. **Carga de Usuarios en Base de Datos:** En la misma pantalla, registra a los estudiantes en la tabla `USUARIO` indicando `nombre`, `correo`, `contrasena` (cifrada con BCrypt por el servicio), `rol_sistema = 'ESTUDIANTE'`, `id_curso` y `activo = true`.
3. **Creación de Reto:** El Administrador accede a **Retos y Grupos** (`/admin/grupos-retos`) y crea un nuevo `RETO` vinculado al `id_curso` con `nombre`, `descripcion` y `estado_reto = 'ACTIVO'`.
4. **Creación de Grupos/Tableros del Reto:** Crea un `GRUPO` vinculado al `id_reto`:
   - El servicio persiste el `GRUPO` y **genera automáticamente 3 fases iniciales en `FASE`** (*1. Análisis y Diseño*, *2. Desarrollo*, *3. Pruebas y Despliegue*).
5. **Asignación de Miembros al Grupo:** Selecciona los estudiantes del curso y los registra en `MIEMBRO_GRUPO` con rol inicial `COLABORADOR`.

### Flujos Alternativos
- **1a / 2a. Duplicidad de Curso o Correo:** Si el nombre del curso o el correo ya existen, el sistema notifica el conflicto sin interrumpir la ejecución.
- **5a. Validación de coherencia:** El servicio valida que los alumnos pertenezcan al mismo curso del reto y no estén asignados a otro grupo en el mismo reto activo.
- **6a. Archivo de promociones pasadas:** Si el Administrador archiva un curso o desactiva a un alumno, actualiza `activo = false`. El usuario ya no puede iniciar sesión, pero se conserva la integridad de sus registros históricos.

### Postcondiciones
- Estructura académica persistida y lista para la interacción del alumnado.

---

## CU-03: Asignación de Roles Internos ETHAZI (Informativo)

### Descripción
Los estudiantes de un grupo eligen su función organizativa (`GESTOR`, `ANALISTA`, `DESARROLLADOR` o `COLABORADOR`). Son etiquetas informativas y visuales para el seguimiento pedagógico sin restringir permisos sobre el tablero.

### Flujo Principal
1. El Estudiante accede a **Mi Equipo** (`/grupo/equipo`).
2. El sistema presenta los integrantes del grupo activo y sus roles asignados.
3. El Estudiante selecciona su rol en el desplegable y guarda los cambios.
4. El sistema actualiza `MIEMBRO_GRUPO.rol_en_grupo` y refresca la vista con el badge actualizado.

---

## CU-04: Estructuración del Tablero en Fases

### Descripción
Permite a los integrantes del equipo añadir fases complementarias a las 3 iniciales, reordenarlas o suprimir aquellas que no contengan tareas.

### Flujo Principal
1. El Estudiante accede a **Fases del Reto** (`/grupo/fases`).
2. El sistema lista las fases del grupo ordenadas ascendentemente por `orden`.
3. El Estudiante define `nombre_fase` y `orden` ($\ge 1$) y pulsa "Añadir Fase".
4. El sistema registra la nueva fila en `FASE`.

### Flujos Alternativos
- **4a. Eliminación de fase con tareas:** Si se solicita eliminar una fase con tareas asociadas (`existsByFaseId`), el servicio rechaza la operación informando: *"No puedes eliminar una fase que contiene tareas; muévelas o elimínalas primero"*. Si está vacía, procede a su borrado físico.

---

## CU-05: Ciclo de Vida Kanban y Auditoría de Tareas

### Descripción
Gestiona el flujo completo de trabajo: alta y baja de tareas, asignación de responsable individual, transiciones de columna con registro automático de tiempos, bloqueos justificados y comentarios trazables en `LOG_TAREA`.

### Precondiciones
1. El usuario pertenece al `GRUPO` en `MIEMBRO_GRUPO`.
2. El `RETO` asociado al grupo se encuentra en estado `ACTIVO`.

### Subflujos Operativos

#### A. Creación de Tarea (`POST /tablero/tareas`)
1. El Estudiante introduce `titulo`, selecciona `id_fase` y opcionalmente `descripcion`, `id_asignado` y `fecha_esperada_finalizacion`.
2. El servicio guarda la tarea en estado `PENDIENTE` con `fechaCreacionReal = LocalDateTime.now()` y registra un evento `CREACION` en `LOG_TAREA`.

#### B. Asignación de Responsable Individual (`POST /tablero/tareas/{id}/asignar`)
1. En el modal de detalle, se asigna un miembro del equipo como titular único (`id_asignado`).

#### C. Transición de Estado y Captura de Tiempos (`PATCH /api/tareas/{id}/estado`)
1. El usuario arrastra la tarjeta a otra columna:
   - **Regla de Responsable Obligatorio:** Si el nuevo estado $\ne$ `PENDIENTE` y `id_asignado` es nulo, se responde HTTP 400 y la tarjeta retorna a `PENDIENTE`.
   - **Tiempo de Inicio:** Al salir de `PENDIENTE`, si `fecha_inicializacion` es nula, se registra `LocalDateTime.now()`.
   - **Tiempo de Finalización:** Al ingresar a `COMPLETADA`, se establece `fecha_finalizacion_real = LocalDateTime.now()`; si se reabre, se resetea a `null`.
2. Se registra un evento `TRANSICION` en `LOG_TAREA`.

#### D. Bloqueo Justificado (`POST /api/tareas/{id}/bloquear`)
1. Al soltar una tarea en `BLOQUEADA`, se solicita obligatoriamente el motivo técnico.
2. En una transacción atómica, se actualiza el estado a `BLOQUEADA` y se registra un evento `BLOQUEO` con el comentario explicativo en `LOG_TAREA`.

#### E. Comentarios de Seguimiento (`POST /tablero/tareas/{id}/comentarios`)
1. Se registra un evento `COMENTARIO` en `LOG_TAREA` accesible desde el historial de la tarjeta.

#### F. Descarte de Tarea Errónea (`POST /tablero/tareas/{id}/eliminar`)
1. La tarea y sus eventos asociados se eliminan en cascada (`CascadeType.ALL, orphanRemoval = true`).

---

## CU-06: Cierre de Sesión Seguro

### Flujo Principal
1. El usuario solicita la finalización de sesión (`POST /logout`).
2. Spring Security destruye la sesión HTTP y redirige a `/login?logout=true`.

---

## CU-07: Consulta de Historial de Retos Finalizados (Solo Lectura)

### Descripción
Permite al alumnado consultar tableros de retos anteriores evaluados, preservando las tareas, comentarios y la formación del equipo en aquel momento.

### Flujo Principal
1. El Estudiante accede a su historial de retos.
2. El sistema consulta las participaciones con `EstadoReto.FINALIZADO`.
3. Al seleccionar un proyecto, el sistema valida la pertenencia previa y carga el tablero en modo solo lectura (`soloLectura = true`).

---

## CU-08: Cierre Académico de Reto

### Descripción
Permite al Administrador dar por concluido un reto (`ACTIVO` $\rightarrow$ `FINALIZADO`), congelando de forma instantánea todos los tableros de ese reto para su posterior consulta histórica.

### Flujo Principal
1. El Administrador pulsa "Finalizar Reto" en el panel de control.
2. El servicio actualiza `reto.estadoReto = FINALIZADO`.
3. Todos los grupos y miembros asociados quedan automáticamente disponibles para el historial en solo lectura, permitiendo conformar nuevos equipos para el siguiente reto.
