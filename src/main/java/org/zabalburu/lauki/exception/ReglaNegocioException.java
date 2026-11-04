package org.zabalburu.lauki.exception;

/**
 * Regla de negocio de un solo uso. Única excepción donde el servicio escribe el mensaje.
 * Si el mismo texto aparece en un segundo throw, promocionarla a excepción propia.
 */
public class ReglaNegocioException extends BusinessException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}