package org.zabalburu.lauki.exception;

/**
 * El responsable asignado a la tarea existe, pero no es miembro del grupo del tablero.
 */
public class ResponsableAjenoAlGrupoException extends BusinessException {

    public ResponsableAjenoAlGrupoException(
    		Integer idUsuario, 
    		Integer idGrupo) {
        super("El usuario %d no pertenece al grupo %d y no puede ser responsable".formatted(
        		idUsuario, 
        		idGrupo));
    }
}