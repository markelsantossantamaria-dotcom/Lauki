package org.zabalburu.lauki.exception;

public class RetoYaFinalizadoException extends BusinessException {

    public RetoYaFinalizadoException(Integer idReto) {
        super("El reto %d ya se encuentra finalizado".formatted(idReto));
    }
}