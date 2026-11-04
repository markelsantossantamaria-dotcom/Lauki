package org.zabalburu.lauki.exception;

/**
 * El alumno ya está en otro grupo del mismo reto.
 * La UNIQUE(id_grupo, id_usuario) solo cubre el mismo grupo; esta regla cubre todo el reto.
 */
public class UsuarioYaAsignadoAlRetoException extends BusinessException {

    public UsuarioYaAsignadoAlRetoException(
    		Integer idUsuario, 
    		Integer idReto) {
        super("El usuario %d ya forma parte de un grupo del reto %d".formatted(
        		idUsuario, 
        		idReto));
    }
}