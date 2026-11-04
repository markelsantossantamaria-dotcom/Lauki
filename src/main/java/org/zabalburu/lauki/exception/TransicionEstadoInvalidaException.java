package org.zabalburu.lauki.exception;

import org.zabalburu.lauki.domain.enums.EstadoTarea;

/**
 * Cambio de estado de tarea rechazado. Una variante por causa, cada una con su mensaje.
 * Uso: throw TransicionEstadoInvalidaException.yaBloqueada();
 * Para añadir una causa nueva: método estático nuevo, el constructor sigue privado.
 */
public class TransicionEstadoInvalidaException extends BusinessException {

    private TransicionEstadoInvalidaException(String mensaje) {
        super(mensaje);
    }

    public static TransicionEstadoInvalidaException mismoEstado(EstadoTarea estado) {
        return new TransicionEstadoInvalidaException("La tarea ya está en el estado %s".formatted(estado));
    }

    public static TransicionEstadoInvalidaException bloqueoSinMotivo() {
        return new TransicionEstadoInvalidaException(
                "Para bloquear una tarea use la acción de bloqueo e indique el motivo");
    }

    public static TransicionEstadoInvalidaException yaBloqueada() {
        return new TransicionEstadoInvalidaException("La tarea ya está bloqueada");
    }

    /** Reservada para una futura matriz de transiciones. */
    public static TransicionEstadoInvalidaException noPermitida(
    		EstadoTarea origen, 
    		EstadoTarea destino) {
        return new TransicionEstadoInvalidaException("Transición no permitida: %s → %s".formatted(
        		origen, 
        		destino));
    }
}