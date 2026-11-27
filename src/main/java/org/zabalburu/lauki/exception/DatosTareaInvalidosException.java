package org.zabalburu.lauki.exception;

public class DatosTareaInvalidosException extends BusinessException {

    private DatosTareaInvalidosException(String mensaje) {
        super(mensaje);
    }

    public static DatosTareaInvalidosException faseObligatoria() {
        return new DatosTareaInvalidosException("La fase es obligatoria");
    }

    public static DatosTareaInvalidosException estadoObligatorio() {
        return new DatosTareaInvalidosException("El estado es obligatorio");
    }

    public static DatosTareaInvalidosException comentarioVacio() {
        return new DatosTareaInvalidosException("El comentario no puede estar vacío");
    }

    public static DatosTareaInvalidosException identificadorObligatorio() {
        return new DatosTareaInvalidosException("El identificador de la tarea es obligatorio");
    }

    public static DatosTareaInvalidosException tituloObligatorio() {
        return new DatosTareaInvalidosException("El título es obligatorio");
    }

    public static DatosTareaInvalidosException tituloDemasiadoLargo(int longitudMaxima) {
        return new DatosTareaInvalidosException(
                "El título no puede superar los %d caracteres".formatted(longitudMaxima));
    }
}
