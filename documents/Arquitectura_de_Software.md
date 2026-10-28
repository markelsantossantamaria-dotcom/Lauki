# 🏗️ Arquitectura de Software (Software Architecture) — Lauki

[⬅️ Volver al README principal](../README.md)

---

## 1. Diagrama de Arquitectura General (Frontend, Backend y Base de Datos)

**Lauki** implementa una arquitectura **Monolito Modular MVC** renderizada en servidor con **Spring Boot 3 y Thymeleaf**, complementada con un controlador REST interno (`TareaApiController`) para las operaciones interactivas Drag & Drop del tablero Kanban.

```mermaid
flowchart TD
    subgraph Cliente["Capa Cliente (Navegador Web)"]
        UI["Thymeleaf HTML5 + Bootstrap 5 (Vistas y Formularios)"]
        JS["kanban.js (< 60 lineas) - Drag and Drop, Modal Bloqueo y Filtro Fase"]
    end

    subgraph Config["Seguridad y Arranque (com.zabalburu.lauki.config)"]
        SEC["SecurityConfig (/login unificado y redireccion por Rol)"]
        INIT["DataInitializer (Precarga exclusiva de ADMINISTRADOR si no existe)"]
    end

    subgraph Web["Capa Web y Controladores (com.zabalburu.lauki.controller)"]
        AUTH_CTRL["AuthController (/login, /logout)"]
        ADMIN_CTRL["AdminController (/admin/** - Cursos, Usuarios, Grupos y Retos)"]
        GRUPO_CTRL["GrupoFaseController (/grupo/** - Equipo ETHAZI, Fases e Historial)"]
        EST_CTRL["TableroController (/tablero/** - Kanban Activo o Historico Solo Lectura)"]
        API_CTRL["TareaApiController (REST /api/tareas/** - Drag and Drop y Bloqueo)"]
    end

    subgraph DTO["Capa DTO con Factorias Estaticas (com.zabalburu.lauki.dto)"]
        DTOS["TareaDTO.from(Tarea) y LogTareaDTO.from(LogTarea)"]
        REQUESTS["6 Request Records con Jakarta Validation"]
    end

    subgraph Servicios["Capa de Negocio (com.zabalburu.lauki.service)"]
        USER_SRV["CustomUserDetailsService (Login contra tabla USUARIO)"]
        ADMIN_CU_SRV["AdminCursoUsuarioService (Alta Cursos y Usuarios con BCrypt)"]
        ADMIN_GR_SRV["AdminGrupoRetoService (Retos, Grupos con 3 Fases base y Cierre)"]
        EQUIPO_SRV["EquipoFaseService (Rol ETHAZI, Fases e Historial)"]
        KANBAN_SRV["KanbanTareaService (Transactional + Validacion de Grupo)"]
    end

    subgraph Dominio["Capa de Dominio y Persistencia (domain y repository)"]
        ENTITIES["8 Entidades JPA simples (PK Integer)"]
        REPOS["8 Interfaces JpaRepository (Entidad, Integer)"]
    end

    subgraph Datos["Capa de Datos Relacional"]
        DB[("MySQL / PostgreSQL en Docker - 8 Tablas")]
    end

    UI -->|"GET / POST (Formularios MVC)"| AUTH_CTRL
    UI -->|"GET / POST (Formularios MVC)"| ADMIN_CTRL
    UI -->|"GET / POST (Formularios MVC)"| GRUPO_CTRL
    UI -->|"GET / POST (Crear Tarea, Asignar, Comentar)"| EST_CTRL
    JS -->|"Fetch JSON (Cambio de Estado y Bloqueo)"| API_CTRL

    SEC --> USER_SRV
    INIT --> REPOS
    AUTH_CTRL --> USER_SRV
    ADMIN_CTRL --> ADMIN_CU_SRV
    ADMIN_CTRL --> ADMIN_GR_SRV
    GRUPO_CTRL --> EQUIPO_SRV
    EST_CTRL --> KANBAN_SRV
    API_CTRL --> KANBAN_SRV

    KANBAN_SRV -->|"Convierte con Record.from(Entity)"| DTOS
    DTOS --> EST_CTRL
    DTOS --> API_CTRL

    USER_SRV --> REPOS
    ADMIN_CU_SRV --> REPOS
    ADMIN_GR_SRV --> REPOS
    EQUIPO_SRV --> REPOS
    KANBAN_SRV --> REPOS

    REPOS --> ENTITIES
    ENTITIES --> DB
```

---

## 2. Diagrama Entidad-Relación (DER)

> 📄 Para consultar el diccionario completo de columnas, tipos SQL/JPA y la justificación de cada relación `1:N` y `N:M`, visita **[`Modelo_de_Base_de_Datos.md`](Modelo_de_Base_de_Datos.md)**.

```mermaid
erDiagram
    CURSO ||--o{ USUARIO : "id_curso (nullable en ADMIN)"
    CURSO ||--o{ RETO : "id_curso"

    RETO ||--o{ GRUPO : "id_reto (1 Grupo = 1 Tablero del Reto)"
    GRUPO ||--o{ MIEMBRO_GRUPO : "id_grupo"
    USUARIO ||--o{ MIEMBRO_GRUPO : "id_usuario"

    GRUPO ||--o{ FASE : "id_grupo"
    FASE ||--o{ TAREA : "id_fase"

    USUARIO ||--o{ TAREA : "id_asignado (responsable individual)"
    TAREA ||--o{ LOG_TAREA : "id_tarea"
    USUARIO ||--o{ LOG_TAREA : "id_usuario (autor)"

    CURSO {
        int id PK
        string nombre "UNIQUE"
        boolean activo
    }
    USUARIO {
        int id PK
        int id_curso FK "Nullable en ADMIN"
        string nombre
        string correo "UNIQUE"
        string contrasena "BCrypt"
        string rol_sistema "ADMINISTRADOR, ESTUDIANTE"
        boolean activo
    }
    RETO {
        int id PK
        int id_curso FK
        string nombre
        text descripcion
        string estado_reto "ACTIVO, INACTIVO, FINALIZADO"
    }
    GRUPO {
        int id PK "Representa al Equipo y su Tablero Kanban"
        int id_reto FK
        string nombre
        timestamp fecha_creacion
    }
    MIEMBRO_GRUPO {
        int id PK
        int id_grupo FK "UNIQUE(id_grupo, id_usuario)"
        int id_usuario FK
        string rol_en_grupo "GESTOR, ANALISTA, DESARROLLADOR, COLABORADOR"
    }
    FASE {
        int id PK
        int id_grupo FK
        string nombre_fase
        int orden
    }
    TAREA {
        int id PK
        int id_fase FK
        int id_asignado FK "Nullable en PENDIENTE"
        string titulo
        text descripcion
        string estado "PENDIENTE, EN_PROGRESO, BLOQUEADA, EN_SUPERVISION, COMPLETADA"
        timestamp fecha_creacion_real
        timestamp fecha_inicializacion "Nullable"
        date fecha_esperada_finalizacion "Nullable"
        timestamp fecha_finalizacion_real "Nullable"
    }
    LOG_TAREA {
        int id PK
        int id_tarea FK
        int id_usuario FK
        string tipo_evento "CREACION, TRANSICION, BLOQUEO, COMENTARIO"
        string estado_anterior "Nullable"
        string estado_nuevo "Nullable"
        text comentario
        timestamp fecha_registro
    }
```

---

## 3. Diagrama de Flujo del Usuario (User Flow)

```mermaid
sequenceDiagram
    participant App as Spring Boot (DataInitializer)
    participant BDD as Base de Datos (JPA)
    actor Admin as Profesor (ADMINISTRADOR)
    actor Alumno as Alumno (ESTUDIANTE)

    Note over App,BDD: PASO 0 - Arranque Inicial del Sistema
    App->>BDD: Comprueba existsByCorreo("admin@zabalburu.org")
    BDD-->>App: false -> Inserta cuenta ADMINISTRADOR con BCrypt

    Note over Admin,BDD: PASO 1 - Login Admin y Carga de Curso y Usuarios
    Admin->>BDD: Inicia sesion en /login -> Redirige a /admin/cursos-usuarios
    Admin->>BDD: Crea CURSO ("2 DAW 2026/27") y da de alta a los ESTUDIANTES en USUARIO
    Admin->>BDD: Crea RETO 1 (ACTIVO) y crea GRUPO 1 asignando a los alumnos en MIEMBRO_GRUPO
    BDD-->>BDD: Genera automaticamente 3 FASES iniciales para el GRUPO 1

    Note over Alumno,BDD: PASO 2 - Operativa Kanban del Reto 1
    Alumno->>BDD: Inicia sesion en /login -> Redirige a /tablero (GRUPO 1)
    Alumno->>BDD: Crea Tareas, asigna responsable y mueve tarjetas (Drag and Drop)
    BDD-->>BDD: Registra cada evento y bloqueo justificado en LOG_TAREA

    Note over Admin,Alumno: PASO 3 - Cierre de Reto 1 y Rotacion al Reto 2
    Admin->>BDD: Pulsa Finalizar Reto 1 -> RETO 1 pasa a FINALIZADO
    Admin->>BDD: Crea RETO 2 (ACTIVO) y crea nuevos GRUPOS con distinta combinacion de alumnos
    Alumno->>BDD: En /tablero ve el nuevo tablero del RETO 2 (Editable)
    Alumno->>BDD: En /tablero/historial consulta su GRUPO 1 del RETO 1 en MODO SOLO LECTURA
```

---

## 4. Estructura de Paquetes y Componentes del Proyecto (`~44 Archivos`)

```text
lauki/
├── pom.xml
├── docker-compose.yml
└── src/main/
    ├── java/com/zabalburu/lauki/
    │   ├── LaukiApplication.java
    │   ├── config/
    │   │   ├── SecurityConfig.java              # Login único (/login), redirección por rol y CSRF ignorado en /api/**
    │   │   └── DataInitializer.java             # Precarga exclusiva de la cuenta ADMINISTRADOR inicial
    │   ├── domain/                              # 8 Entidades JPA con PK Integer + 5 Enums
    │   │   ├── Curso.java, Usuario.java, Reto.java, Grupo.java, MiembroGrupo.java, Fase.java, Tarea.java, LogTarea.java
    │   │   └── enums/ (RolSistema, RolEnGrupo, EstadoReto, EstadoTarea, TipoEventoLog)
    │   ├── dto/                                 # Records con factoría estática .from(Entity) y Jakarta Validation
    │   │   ├── TareaDTO.java, LogTareaDTO.java
    │   │   └── requests/ (CrearUsuarioRequest, CrearTareaRequest, CambiarEstadoTareaRequest, BloqueoTareaRequest, ComentarioRequest, AsignarRolRequest)
    │   ├── repository/                          # 8 Interfaces JpaRepository<Entidad, Integer>
    │   ├── service/                             # 5 Servicios de Negocio (@Transactional)
    │   │   ├── CustomUserDetailsService.java
    │   │   ├── AdminCursoUsuarioService.java
    │   │   ├── AdminGrupoRetoService.java
    │   │   ├── EquipoFaseService.java
    │   │   └── KanbanTareaService.java
    │   └── controller/                          # 4 Controladores MVC + 1 Controlador REST
    │       ├── AuthController.java, AdminController.java, GrupoFaseController.java, TableroController.java
    │       └── TareaApiController.java
    └── resources/
        ├── application.properties
        ├── static/ (css/custom.css, js/kanban.js)
        └── templates/ (fragments/, auth/login.html, admin/, estudiante/)
```
