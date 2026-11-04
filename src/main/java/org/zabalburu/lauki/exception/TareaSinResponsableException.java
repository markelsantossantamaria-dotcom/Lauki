package org.zabalburu.lauki.exception;

/**
 * Regla "responsable obligatorio para salir de PENDIENTE".
 * Se lanza desde cambiarEstado y bloquear.
 */
public class TareaSinResponsableException extends BusinessException {

    public TareaSinResponsableException(Integer idTarea) {
        super("La tarea %d necesita un responsable antes de salir de PENDIENTE".formatted(idTarea));
    }
}                        