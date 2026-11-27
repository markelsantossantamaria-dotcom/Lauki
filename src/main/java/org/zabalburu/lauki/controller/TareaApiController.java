package org.zabalburu.lauki.controller;

import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.zabalburu.lauki.dto.request.CambiarEstadoTareaRequest;
import org.zabalburu.lauki.dto.response.TareaResponse;
import org.zabalburu.lauki.service.KanbanTareaService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/estudiante/tareas")
public class TareaApiController {

    private final KanbanTareaService kanbanTareaService;

    public TareaApiController(KanbanTareaService kanbanTareaService) {
        this.kanbanTareaService = kanbanTareaService;
    }

    @PostMapping(path = "/{id}/mover", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public TareaResponse moverTarea(Authentication authentication, 
    		@PathVariable("id") Integer id,
            @Valid 
            @ModelAttribute CambiarEstadoTareaRequest solicitud) {
        return kanbanTareaService.cambiarEstado(authentication.getName(), id, solicitud);
    }
}
