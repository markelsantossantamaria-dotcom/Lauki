package org.zabalburu.lauki.exception;

/**
 * El curso del usuario no coincide con el curso del reto.
 * También salta con un ADMINISTRADOR (su id_curso es nulo).
 */
public class UsuarioFueraDelCursoException extends BusinessException {

    public UsuarioFueraDelCursoException(
    		Integer idUsuario, 
    		Integer idReto) {
        super("El usuario %d no pertenece al curso del reto %d".formatted(
        		idUsuario, 
        		idReto));
    }
}