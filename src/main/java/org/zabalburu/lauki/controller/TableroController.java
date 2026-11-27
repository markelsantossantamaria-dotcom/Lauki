package org.zabalburu.lauki.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zabalburu.lauki.domain.Curso;
import org.zabalburu.lauki.domain.MiembroGrupo;
import org.zabalburu.lauki.domain.enums.EstadoTarea;
import org.zabalburu.lauki.domain.enums.TipoEventoLog;
import org.zabalburu.lauki.dto.request.BloqueoTareaRequest;
import org.zabalburu.lauki.dto.request.ComentarioRequest;
import org.zabalburu.lauki.dto.request.CrearTareaRequest;
import org.zabalburu.lauki.dto.response.LogTareaResponse;
import org.zabalburu.lauki.dto.response.TareaResponse;
import org.zabalburu.lauki.exception.RecursoNoEncontradoException;
import org.zabalburu.lauki.service.EquipoFaseService;
import org.zabalburu.lauki.service.KanbanTareaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class TableroController {

    private static final String VISTA_TABLERO = "estudiante/tablero";
    private static final String REDIRECCION_TABLERO = "redirect:/estudiante/tablero";

    private final EquipoFaseService equipoFaseService;
    private final KanbanTareaService kanbanTareaService;

    public record FaseVista(
    		Integer id, 
    		Integer numeroOrden, 
    		String nombre
    		) {}

    public record ResponsableVista(
    		Integer id, 
    		String nombreCompleto, 
    		String iniciales
    		) {}

    public record TareaVista(
    		Integer id, 
    		String titulo, 
    		String descripcion, 
    		FaseVista fase,
            ResponsableVista responsable, 
            LocalDateTime fechaCreacion, 
            String motivoBloqueo,
            long totalComentarios, 
            List<LogTareaResponse> comentarios
            ) {}

    @GetMapping({"/tablero", "/estudiante/tablero"})
    public String tablero(Authentication authentication, Model model) {
        String correo = authentication.getName();
        Integer idGrupo;
        try {
            MiembroGrupo miembroActivo = equipoFaseService.obtenerGrupoActivo(correo);
            idGrupo = miembroActivo.getGrupo().getId();
            if (miembroActivo.getUsuario() != null && miembroActivo.getUsuario().getCurso() != null) {
                Curso c = miembroActivo.getUsuario().getCurso();
                String infoCurso = c.getNombre() + (c.getPeriodoAcademico() != null ? " · " + c.getPeriodoAcademico() : "");
                model.addAttribute("cursoEstudiante", infoCurso);
            }
        } catch (RecursoNoEncontradoException exception) {
            model.addAttribute("fases", List.of());
            model.addAttribute("miembrosEquipo", List.of());
            agregarColumnas(model, Map.of());
            model.addAttribute("toastError", "No tienes un grupo con un reto activo");
            return VISTA_TABLERO;
        }

        List<FaseVista> fases = equipoFaseService.listarFases(correo).stream()
                .map(fase -> new FaseVista(fase.id(), 
                		fase.orden(), 
                		fase.nombreFase()))
                .toList();
        
        List<ResponsableVista> miembrosEquipo = equipoFaseService
        		.listarMiembros(correo)
        		.stream()
                .map(miembro -> new ResponsableVista(miembro.usuarioId(),
                		miembro.nombreCompleto(), 
                		miembro.inicial()))
                .toList();
        
        Map<Integer, ResponsableVista> miembrosPorId = miembrosEquipo.stream()
                .collect(Collectors.toMap(ResponsableVista::id, Function.identity()));
        Map<EstadoTarea, List<TareaVista>> tareasPorEstado = kanbanTareaService
        		.listarTareas(correo, idGrupo).stream()
                .collect(Collectors.groupingBy(TareaResponse::estado,
                        Collectors.mapping(tarea -> toTareaVista(tarea, miembrosPorId), 
                        Collectors.toList())));

        model.addAttribute("fases", fases);
        model.addAttribute("miembrosEquipo", miembrosEquipo);
        agregarColumnas(model, tareasPorEstado);
        return VISTA_TABLERO;
    }

    @PostMapping("/estudiante/tareas/crear")
    public String crearTarea(Authentication authentication, @Valid @ModelAttribute CrearTareaRequest solicitud,
            RedirectAttributes redirectAttributes) {
        kanbanTareaService.crearTarea(authentication.getName(), solicitud);
        return confirmarCambio(redirectAttributes, "Tarea creada correctamente");
    }

    @PostMapping("/estudiante/tareas/editar")
    public String editarTarea(Authentication authentication, 
    		@RequestParam("id") Integer id,
            @RequestParam("titulo") String titulo, 
            @RequestParam("faseId") Integer faseId,
            @RequestParam(name = "responsableId", required = false) Integer responsableId,
            RedirectAttributes redirectAttributes) {
        kanbanTareaService.editarTarea(authentication.getName(), id, titulo, faseId, responsableId);
        return confirmarCambio(redirectAttributes, "Tarea actualizada correctamente");
    }

    @PostMapping("/estudiante/tareas/eliminar")
    public String eliminarTarea(Authentication authentication, 
    		@RequestParam("id") Integer id,
            RedirectAttributes redirectAttributes) {
        kanbanTareaService.eliminarTarea(authentication.getName(), id);
        return confirmarCambio(redirectAttributes, "Tarea eliminada correctamente");
    }

    @PostMapping("/estudiante/tareas/descripcion")
    public String actualizarDescripcion(Authentication authentication, 
    		@RequestParam("id") Integer id,
            @RequestParam("descripcion") String descripcion, 
            RedirectAttributes redirectAttributes) {
        kanbanTareaService.actualizarDescripcion(authentication.getName(), id, descripcion);
        return confirmarCambio(redirectAttributes, "Descripción actualizada correctamente");
    }

    @PostMapping("/estudiante/tareas/comentar")
    public String comentar(Authentication authentication, 
    		@RequestParam("tareaId") Integer tareaId,
            @RequestParam("contenido") String contenido, 
            RedirectAttributes redirectAttributes) {
        kanbanTareaService.comentar(authentication.getName(), tareaId, new ComentarioRequest(contenido));
        return confirmarCambio(redirectAttributes, "Comentario añadido correctamente");
    }

    @PostMapping("/estudiante/tareas/bloquear")
    public String bloquear(Authentication authentication, @Valid @ModelAttribute BloqueoTareaRequest solicitud,
            RedirectAttributes redirectAttributes) {
        kanbanTareaService.bloquear(authentication.getName(), solicitud);
        return confirmarCambio(redirectAttributes, "Tarea bloqueada correctamente");
    }

    private static void agregarColumnas(Model model, Map<EstadoTarea, List<TareaVista>> tareasPorEstado) {
        model.addAttribute("tareasPendientes", tareasPorEstado.getOrDefault(EstadoTarea.PENDIENTE, List.of()));
        model.addAttribute("tareasEnProgreso", tareasPorEstado.getOrDefault(EstadoTarea.EN_PROGRESO, List.of()));
        model.addAttribute("tareasEnSupervision", tareasPorEstado.getOrDefault(EstadoTarea.EN_SUPERVISION, List.of()));
        model.addAttribute("tareasCompletadas", tareasPorEstado.getOrDefault(EstadoTarea.COMPLETADA, List.of()));
        model.addAttribute("tareasBloqueadas", tareasPorEstado.getOrDefault(EstadoTarea.BLOQUEADA, List.of()));
    }

    private static TareaVista toTareaVista(
    		TareaResponse tarea, 
    		Map<Integer, ResponsableVista> miembrosPorId) {
        FaseVista fase = new FaseVista(tarea.faseId(), 
        		tarea.faseOrden(), 
        		tarea.faseNombre());
        ResponsableVista responsable = null;
        if (tarea.responsableId() != null) {
            ResponsableVista miembro = miembrosPorId.get(tarea.responsableId());
            String nombre = miembro != null ? miembro.nombreCompleto() : "Responsable fuera del equipo";
            responsable = new ResponsableVista(tarea.responsableId(), nombre, tarea.responsableInicial());
        }
        List<LogTareaResponse> comentarios = tarea.logs().stream()
                .filter(log -> log.tipoEvento() == TipoEventoLog.COMENTARIO)
                .toList();
        return new TareaVista(tarea.id(), 
        		tarea.titulo(), 
        		tarea.descripcion(), 
        		fase, 
        		responsable,
                tarea.fechaCreacion(), 
                tarea.ultimoMotivoBloqueo(), 
                tarea.totalComentarios(), 
                comentarios);
    }

    private static String confirmarCambio(RedirectAttributes redirectAttributes, String mensaje) {
        redirectAttributes.addFlashAttribute("toastExito", mensaje);
        return REDIRECCION_TABLERO;
    }
}
