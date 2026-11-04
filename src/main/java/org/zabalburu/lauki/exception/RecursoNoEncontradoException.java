package org.zabalburu.lauki.exception;

import lombok.Getter;

/**
 * Se ha referenciado una entidad que no existe (findById vacío).
 * Uso: orElseThrow(() -> new RecursoNoEncontradoException(Curso.class, id))
 */
@Getter
public class RecursoNoEncontradoException extends BusinessException {

    private final Class<?> recurso;
    private final Integer id;

    public RecursoNoEncontradoException(
    		Class<?> recurso, 
    		Integer id) {
        super("No existe %s con id %d".formatted(
        		recurso.getSimpleName(), 
        		id));
        this.recurso = recurso;
        this.id = id;
    }
}