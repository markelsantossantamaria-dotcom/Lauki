# 🗂️ Lauki — Mapeando el Caos

![Java 21](https://img.shields.io/badge/Java-21_LTS-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot 3](https://img.shields.io/badge/Spring_Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring Data JPA](https://img.shields.io/badge/Hibernate-JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![Thymeleaf](https://img.shields.io/badge/Thymeleaf-SSR-005F0F?style=for-the-badge&logo=thymeleaf&logoColor=white)
![Bootstrap 5](https://img.shields.io/badge/Bootstrap-5-7952B3?style=for-the-badge&logo=bootstrap&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Ready-2496ED?style=for-the-badge&logo=docker&logoColor=white)

> **Centro:** Zabalburu Ikastetxea — 2º DAW (Desarrollo de Aplicaciones Web)  
> **Metodología:** Aprendizaje Colaborativo Basado en Retos (**ETHAZI**) · **Validación Interciclos:** CFGS ASIR

---

## 1. Visión del Producto (Product Vision)

En el modelo pedagógico **ETHAZI**, los equipos de alumnado deben autoorganizarse para resolver retos técnicos complejos. Sin embargo, es frecuente que surjan dificultades para repartir tareas con titularidad clara, visualizar el estado real del trabajo y detectar bloqueos a tiempo.

**Lauki** es una herramienta web Kanban diseñada para gestionar retos académicos:
- **Organización por Fases y Responsable Individual:** Cada reto se divide en equipos/tableros (`GRUPO`), etapas (`FASE`) y unidades de trabajo (`TAREA`) con un único responsable titular.
- **Gestión Visual de Bloqueos y Trazabilidad:** Las tareas avanzan por 5 columnas interactivas, exigiendo justificar cualquier bloqueo en un registro de auditoría inmutable (`LOG_TAREA`).
- **Continuidad Multi-Curso e Historial:** Permite dar de alta futuros cursos escolares y conserva los tableros de retos finalizados en **modo solo lectura** aunque los alumnos cambien de grupo en el siguiente reto.

---

## 2. Documento de Requisitos de Software (SRS - Software Requirements Specification)

### Requisitos de Aprendizaje y Funcionales

| Código | Requisito | Implementación en Lauki |
| :---: | :--- | :--- |
| **R1** | **Organización del trabajo** | División del reto en grupos/tableros (`GRUPO`), fases (`FASE`) y tareas (`TAREA`) con responsable individual (`id_asignado`). |
| **R2** | **Seguimiento del avance** | Tablero Kanban de 5 estados (`PENDIENTE`, `EN_PROGRESO`, `BLOQUEADA`, `EN_SUPERVISION`, `COMPLETADA`) con filtro rápido por fase. |
| **R3** | **Interacción visual** | Drag & Drop con JavaScript Vanilla (`kanban.js`), modal obligatorio de motivo de bloqueo y filtrado dinámico en cliente. |
| **R4** | **Información persistente** | Base de datos relacional (**8 tablas**) con Spring Data JPA (Hibernate 6), claves `Integer` simples y estados de dominio (`activo`, `EstadoReto`). |
| **R5** | **Acceso controlado** | Login unificado (`/login`) con BCrypt para `ADMINISTRADOR` y `ESTUDIANTE`, redirección por rol y aislamiento por equipo. |
| **R6** | **Calidad y mantenibilidad** | Arquitectura MVC + DTO `record`s, tests unitarios con JUnit 5 + Mockito, Git semántico y contenedores Docker para testeo con ASIR. |

### Restricciones de Negocio
- **Administradores Precargados (`DataInitializer`):** Garantiza la cuenta inicial de `ADMINISTRADOR` para que el equipo docente inicie sesión en `/login` y cargue en la base de datos los cursos y estudiantes.
- **Roles ETHAZI Informativos:** El rol interno del alumno (`GESTOR`, `ANALISTA`, `DESARROLLADOR`, `COLABORADOR`) es un identificador visual organizativo sin restringir permisos en el tablero del equipo.
- **Responsable Obligatorio para Avanzar:** Ninguna tarea puede salir de `PENDIENTE` si tiene `id_asignado == null`.
- **Bloqueo Justificado Atómico:** Pasar una tarea a `BLOQUEADA` requiere registrar obligatoriamente el motivo en `LOG_TAREA`.

---

## 📂 Documentación Técnica del Proyecto (`documents/`)

Haz clic en cualquiera de las secciones inferiores para abrir su documento técnico completo:

| Sección | Documento en `documents/` | Contenido Principal |
| :--- | :--- | :--- |
| **3. Arquitectura de Software**<br>*(Software Architecture)* | **[`Arquitectura_de_Software.md`](documents/Arquitectura_de_Software.md)**<br>**[`Modelo_de_Base_de_Datos.md`](documents/Modelo_de_Base_de_Datos.md)** | Diagrama general de arquitectura (Frontend, Backend, BDD), Diagrama Entidad-Relación (DER de 8 tablas), User Flows y diccionario de datos en 3FN. |
| **4. Especificación Funcional**<br>*(Functional Specification)* | **[`Casos_de_Uso.md`](documents/Casos_de_Uso.md)**<br>**[`Historias_de_Usuario.md`](documents/Historias_de_Usuario.md)** | Los **8 Casos de Uso (`CU-01` a `CU-08`)** con flujos alternativos y el **Product Backlog de 13 Historias de Usuario (`US-01` a `US-13`)** con criterios BDD. |
| **5. Documentación de APIs**<br>*(API Documentation)* | **[`Documentacion_API.md`](documents/Documentacion_API.md)** | Especificación de los endpoints REST internos (`/api/tareas/**`), payloads de entrada (`Request` records), salidas JSON (`TareaDTO`) y códigos HTTP. |
| **6. Decisiones Arquitectónicas**<br>*(ADRs)* | **[`Decisiones_Arquitectonicas_ADR.md`](documents/Decisiones_Arquitectonicas_ADR.md)** | Justificación formal de los **5 ADRs (`ADR-01` a `ADR-05`)** y guía de prevención de los **8 fallos críticos** durante el desarrollo. |
| **7. Guía de Contribución y Git**<br>*(Contribution & Git Workflow)* | **[`Guia_Contribucion_y_Git.md`](documents/Guia_Contribucion_y_Git.md)** | Flujo de ramas **GitFlow Ligero** (`main`, `develop`, `feat/US-XX`), estándar **Conventional Commits** y comandos diarios para el equipo. |
| **8. Hoja de Ruta**<br>*(Roadmap)* | **[`Roadmap.md`](documents/Roadmap.md)** | Planificación en **5 Fases** con reparto equilibrado (25% por integrante), dependencias críticas e hitos de entrega Docker al alumnado de **ASIR**. |
