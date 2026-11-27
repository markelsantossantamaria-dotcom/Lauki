package org.zabalburu.lauki.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zabalburu.lauki.domain.Grupo;
import org.zabalburu.lauki.domain.Reto;
import org.zabalburu.lauki.domain.Tarea;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.domain.enums.EstadoTarea;
import org.zabalburu.lauki.exception.BusinessException;
import org.zabalburu.lauki.mapper.FaseMapper;
import org.zabalburu.lauki.repository.TareaRepository;
import org.zabalburu.lauki.service.EquipoFaseService;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class GrupoFaseController {

    private final EquipoFaseService equipoFaseService;
    private final TareaRepository tareaRepository;

    public GrupoFaseController(EquipoFaseService equipoFaseService,
                               TareaRepository tareaRepository) {
        this.equipoFaseService = equipoFaseService;
        this.tareaRepository = tareaRepository;
    }

    public record FaseVista(Integer id, Integer numeroOrden, String nombre, Integer tareasActivas) {}

    public record RetoHistorialDTO(Integer id, String codigo, String nombre, String fechaCierre) {}

    public record RetoDetalleDTO(String tituloCompleto, String fechaCierre) {}

    public record TareaHistorialDTO(String titulo, String responsableNombre, String responsableInicial) {}

    @GetMapping({"/grupo/equipo", "/estudiante/equipo", "/equipo"})
    public String equipo(Authentication authentication, Model model) {
        model.addAttribute("miembros", equipoFaseService.listarMiembros(obtenerCorreo(authentication)));
        return "estudiante/equipo";
    }

    @PostMapping({"/grupo/equipo/rol", "/estudiante/equipo/rol"})
    public String actualizarRol(Authentication authentication,
                                @RequestParam Integer usuarioId,
                                @RequestParam String rol,
                                RedirectAttributes redirectAttributes) {
        equipoFaseService.asignarRol(obtenerCorreo(authentication), usuarioId, rol);
        redirectAttributes.addFlashAttribute("mensaje", "Rol actualizado correctamente");
        return "redirect:/grupo/equipo";
    }

    @GetMapping({"/grupo/fases", "/estudiante/fases", "/fases"})
    public String fases(Authentication authentication, Model model) {
        List<FaseVista> fases = equipoFaseService.listarFases(obtenerCorreo(authentication)).stream()
                .map(this::toFaseVista)
                .toList();
        model.addAttribute("fases", fases);
        return "estudiante/fases";
    }

    @PostMapping({"/grupo/fases/crear", "/estudiante/fases/crear"})
    public String crearFase(Authentication authentication,
                            @RequestParam String nombre,
                            @RequestParam(required = false) Integer orden,
                            @RequestParam(name = "numeroOrden", required = false) Integer numeroOrden,
                            RedirectAttributes redirectAttributes) {
        Integer ordenFinal = orden != null ? orden : numeroOrden;
        equipoFaseService.crearFase(obtenerCorreo(authentication), nombre, ordenFinal);
        redirectAttributes.addFlashAttribute("mensaje", "Fase creada correctamente");
        return "redirect:/grupo/fases";
    }

    @PostMapping({"/grupo/fases/eliminar", "/estudiante/fases/eliminar"})
    public String eliminarFase(Authentication authentication,
                               @RequestParam Integer id,
                               RedirectAttributes redirectAttributes) {
        equipoFaseService.eliminarFase(obtenerCorreo(authentication), id);
        redirectAttributes.addFlashAttribute("mensaje", "Fase eliminada correctamente");
        return "redirect:/grupo/fases";
    }

    @PostMapping({"/grupo/fases/editar", "/estudiante/fases/editar"})
    public String editarFase(Authentication authentication,
                             @RequestParam Integer id,
                             @RequestParam String nombre,
                             @RequestParam Integer orden,
                             RedirectAttributes redirectAttributes) {
        equipoFaseService.editarFase(obtenerCorreo(authentication), id, nombre, orden);
        redirectAttributes.addFlashAttribute("mensaje", "Fase actualizada correctamente");
        return "redirect:/grupo/fases";
    }

    @GetMapping({"/grupo/historial", "/estudiante/historial", "/tablero/historial", "/historial"})
    public String historial(Authentication authentication, Model model) {
        List<RetoHistorialDTO> retos = equipoFaseService.listarHistorial(obtenerCorreo(authentication)).stream()
                .map(miembro -> miembro.getGrupo())
                .map(this::toRetoHistorial)
                .toList();
        model.addAttribute("retosFinalizados", retos);
        return "estudiante/historial";
    }

    @GetMapping({
            "/grupo/historial/{idGrupo}",
            "/estudiante/historial/{idGrupo}",
            "/tablero/historial/{idGrupo}"
    })
    public String detalleHistorial(Authentication authentication,
                                   @PathVariable Integer idGrupo,
                                   Model model) {
        Grupo grupo = equipoFaseService.cargarHistorial(obtenerCorreo(authentication), idGrupo);
        Reto reto = grupo.getReto();

        model.addAttribute("reto", new RetoDetalleDTO(
                "Reto " + String.format("%02d", reto.getId()) + " - " + reto.getNombre(),
                ""));
        model.addAttribute("soloLectura", true);

        List<Tarea> tareas = tareaRepository.findAllByFase_Grupo_Id(idGrupo);
        model.addAttribute("tareasPendientes", tareasPorEstado(tareas, EstadoTarea.PENDIENTE));
        model.addAttribute("tareasProgreso", tareasPorEstado(tareas,
                EstadoTarea.EN_PROGRESO, EstadoTarea.EN_SUPERVISION));
        model.addAttribute("tareasCompletadas", tareasPorEstado(tareas, EstadoTarea.COMPLETADA));
        model.addAttribute("tareasBloqueadas", tareasPorEstado(tareas, EstadoTarea.BLOQUEADA));

        return "estudiante/historial-detalle";
    }

    @ExceptionHandler(BusinessException.class)
    public String handleBusinessException(BusinessException exception,
                                          HttpServletRequest request,
                                          RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error", exception.getMessage());

        String uri = request.getRequestURI();
        if (uri.contains("/equipo")) {
            return "redirect:/grupo/equipo";
        }
        if (uri.contains("/historial")) {
            return "redirect:/grupo/historial";
        }
        return "redirect:/grupo/fases";
    }

    private String obtenerCorreo(Authentication authentication) {
        if (authentication == null || authentication.getName() == null
                || authentication.getName().isBlank()) {
            throw new IllegalStateException("No hay una autenticación válida");
        }
        return authentication.getName();
    }

    private FaseVista toFaseVista(FaseMapper.FaseDTO fase) {
        Integer tareasActivas = tareaRepository.findAllByFase_Id(fase.id()).size();
        return new FaseVista(fase.id(), fase.orden(), fase.nombreFase(), tareasActivas);
    }

    private RetoHistorialDTO toRetoHistorial(Grupo grupo) {
        Reto reto = grupo.getReto();
        Integer retoId = reto != null ? reto.getId() : grupo.getId();
        String nombre = reto != null ? reto.getNombre() : grupo.getNombre();
        String codigo = "RETO " + String.format("%02d", retoId);
        String fechaCierre = grupo.getFechaCreacion() != null
                ? grupo.getFechaCreacion().toLocalDate().toString()
                : "";
        return new RetoHistorialDTO(grupo.getId(), codigo, nombre, fechaCierre);
    }

    private List<TareaHistorialDTO> tareasPorEstado(List<Tarea> tareas, EstadoTarea... estados) {
        java.util.Set<EstadoTarea> estadosPermitidos = java.util.EnumSet.noneOf(EstadoTarea.class);
        java.util.Collections.addAll(estadosPermitidos, estados);

        return tareas.stream()
                .filter(tarea -> estadosPermitidos.contains(tarea.getEstadoTarea()))
                .map(this::toTareaHistorial)
                .toList();
    }

    private TareaHistorialDTO toTareaHistorial(Tarea tarea) {
        Usuario responsable = tarea.getUsuario();
        String nombre = responsable != null && responsable.getNombre() != null
                ? responsable.getNombre().trim()
                : "Sin asignar";
        String inicial = "?";
        if (responsable != null && responsable.getNombre() != null
                && !responsable.getNombre().trim().isEmpty()) {
            inicial = responsable.getNombre().trim().substring(0, 1).toUpperCase();
        }
        return new TareaHistorialDTO(tarea.getTitulo(), nombre, inicial);
    }
}