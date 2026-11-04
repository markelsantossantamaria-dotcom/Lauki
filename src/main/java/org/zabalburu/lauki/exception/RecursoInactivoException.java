package org.zabalburu.lauki.exception;

import lombok.Getter;

/**
 * Se usa una entidad que existe pero está archivada (activo = false).
 * Uso: new RecursoInactivoException(Curso.class, curso.getId())
 */
@Getter
public class RecursoInactivoException extends BusinessException {

    private final Class<?> recurso;
    private final Integer id;

    public RecursoInactivoException(
    		Class<?> recurso, 
    		Integer id) {
        super("%s con id %d está inactivo y no admite esta operación".formatted(
        		recurso.getSimpleName(), 
        		id));
        this.recurso = recurso;
        this.id = id;
    }
}