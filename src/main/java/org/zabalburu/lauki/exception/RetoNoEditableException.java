package org.zabalburu.lauki.exception;

import org.zabalburu.lauki.domain.enums.EstadoReto;

/**
 * El reto no admite cambios: FINALIZADO (histórico, solo lectura)
 * o no ACTIVO en operaciones de alumnos.
 */
public class RetoNoEditableException extends BusinessException {

    public RetoNoEditableException(
    		Integer idReto, 
    		EstadoReto estado) {
        super("El reto %d no admite modificaciones (estado: %s)".formatted(
        		idReto, 
        		estado));
    }
}