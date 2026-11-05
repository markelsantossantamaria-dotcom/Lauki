package org.zabalburu.lauki.exception;

import lombok.Getter;

/**
 * Se violaría una restricción UNIQUE (curso.nombre, usuario.correo...).
 * Uso: new RecursoDuplicadoException(Usuario.class, "correo", correo)
 */
@Getter
public class RecursoDuplicadoException extends BusinessException {

    private final Class<?> recurso;
    private final String campo;
    private final Object valor;

    public RecursoDuplicadoException(
    		Class<?> recurso, 
    		String campo, 
    		Object valor) {
        super("Ya existe %s con %s '%s'".formatted(
        		recurso.getSimpleName(), 
        		campo, 
        		valor));
        this.recurso = recurso;
        this.campo = campo;
        this.valor = valor;
    }
}