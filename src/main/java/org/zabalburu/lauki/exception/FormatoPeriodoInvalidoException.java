package org.zabalburu.lauki.exception;

public class FormatoPeriodoInvalidoException extends BusinessException {

    public FormatoPeriodoInvalidoException(String periodo) {
        super("El periodo académico '%s' no tiene un formato válido. Debe ser YYYY/YYYY (ej. 2026/2027)".formatted(periodo));
    }
}
