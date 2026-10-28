# 🔌 Documentación de APIs (API Documentation) — Lauki

[⬅️ Volver al README principal](../README.md) · [🏗️ Ir a Arquitectura de Software](Arquitectura_de_Software.md)

---

## 1. Enfoque Híbrido: Spring MVC (SSR) + API REST Interna

Para minimizar la complejidad en el cliente, **Lauki** combina:
- **Rutas MVC (`@Controller`):** Crean tareas, asignan responsable, añaden comentarios y gestionan catálogos mediante formularios HTML `<form method="post">` estándar con redirección (`redirect:/tablero`).
- **API REST Interna (`@RestController` — `TareaApiController`):** Expone los endpoints JSON consumidos por `kanban.js` mediante `fetch()` para el **Drag & Drop de tarjetas** y el **bloqueo justificado**.

---

## 2. Endpoints REST (`TareaApiController` — `/api/tareas/**`)

### 2.1. Cambiar Estado de una Tarea (Drag & Drop)
- **Endpoint:** `PATCH /api/tareas/{id}/estado`
- **Autenticación:** Sesión activa (`ESTUDIANTE` miembro del `GRUPO` propietario de la tarea).
- **Cuerpo de Petición (`CambiarEstadoTareaRequest`):**
  ```json
  {
    "nuevoEstado": "EN_PROGRESO"
  }
  ```
- **Reglas de Negocio Validadas en `KanbanTareaService`:**
  1. Verifica que el usuario en sesión pertenezca al `GRUPO` de la tarea (`403 Forbidden` si no pertenece).
  2. Verifica que el `RETO` asociado al grupo esté en estado `ACTIVO` (`400 Bad Request` si está `FINALIZADO`).
  3. **Responsable obligatorio:** Si `nuevoEstado != PENDIENTE` y `tarea.asignado == null`, rechaza con `400 Bad Request`.
  4. **Captura automática de tiempos:**
     - Si sale de `PENDIENTE` por primera vez (`fechaInicializacion == null`), asigna `LocalDateTime.now()`.
     - Si entra en `COMPLETADA`, asigna `fechaFinalizacionReal = LocalDateTime.now()` (y si sale de `COMPLETADA`, la resetea a `null`).
  5. Registra un evento `TRANSICION` en `LOG_TAREA`.
- **Respuestas HTTP:**
  - `200 OK`: Devuelve el `TareaDTO` actualizado en JSON.
  - `400 Bad Request`: La tarea no tiene responsable asignado o el reto no está activo.
  - `403 Forbidden`: El alumno no pertenece al grupo dueño del tablero.

---

### 2.2. Bloquear Tarea con Motivo Obligatorio
- **Endpoint:** `POST /api/tareas/{id}/bloquear`
- **Autenticación:** Sesión activa (`ESTUDIANTE` miembro del `GRUPO` propietario de la tarea).
- **Cuerpo de Petición (`BloqueoTareaRequest`):**
  ```json
  {
    "motivo": "Bloqueado esperando credenciales del contenedor de base de datos"
  }
  ```
- **Reglas de Negocio Validadas en `KanbanTareaService` (`@Transactional`):**
  1. Valida `@NotBlank` sobre el campo `motivo` y que la tarea tenga responsable asignado (`id_asignado != null`).
  2. Actualiza `tarea.estado = EstadoTarea.BLOQUEADA`.
  3. Inserta atómicamente en `LOG_TAREA` un evento con `tipo_evento = BLOQUEO`, `estado_anterior`, `estado_nuevo = BLOQUEADA` y el `motivo` en la columna `comentario`.
- **Respuestas HTTP:**
  - `200 OK`: Devuelve el `TareaDTO` actualizado incluyendo el `ultimoMotivoBloqueo` y la lista de `logs`.
  - `400 Bad Request`: Motivo vacío o tarea sin responsable.
  - `403 Forbidden`: Usuario no perteneciente al grupo.

---

## 3. Estructura de Salida JSON (`TareaDTO` y `LogTareaDTO`)

```json
{
  "id": 15,
  "idFase": 2,
  "nombreFase": "2. Desarrollo",
  "idAsignado": 5,
  "nombreAsignado": "Leire Etxebarria",
  "titulo": "Implementar servicio de bloqueo transaccional",
  "descripcion": "Validar motivo obligatorio y guardar en LOG_TAREA",
  "estado": "BLOQUEADA",
  "fechaCreacionReal": "2026-10-28T09:00:00",
  "fechaInicializacion": "2026-10-28T10:15:00",
  "fechaEsperadaFinalizacion": "2026-10-30",
  "fechaFinalizacionReal": null,
  "ultimoMotivoBloqueo": "Bloqueado esperando credenciales del contenedor de base de datos",
  "logs": [
    {
      "id": 42,
      "idTarea": 15,
      "nombreUsuario": "Leire Etxebarria",
      "tipoEvento": "BLOQUEO",
      "estadoAnterior": "EN_PROGRESO",
      "estadoNuevo": "BLOQUEADA",
      "comentario": "Bloqueado esperando credenciales del contenedor de base de datos",
      "fechaRegistro": "2026-10-28T11:30:00"
    }
  ]
}
```
