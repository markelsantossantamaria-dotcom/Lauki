package org.zabalburu.lauki.exception;

/**
 * El usuario (quien ejecuta la operación, o el objetivo de la misma) no es miembro del grupo.
 */
public class UsuarioNoPerteneceAlGrupoException extends BusinessException {

    public UsuarioNoPerteneceAlGrupoException(
    		Integer idUsuario, 
    		Integer idGrupo) {
        super("El usuario %d no pertenece al grupo %d".formatted(
        		idUsuario, 
        		idGrupo));
    }
}