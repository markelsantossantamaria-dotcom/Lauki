package org.zabalburu.lauki.exception;

import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.zabalburu.lauki.dto.response.ErrorResponse;

import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;

/**
 * Traduce las excepciones a respuestas JSON. Solo afecta a los @RestController
 * (la API de /api/**); los controladores MVC con Thymeleaf quedan fuera.
 * Spring elige siempre el handler cuyo tipo está más cerca en la herencia.
 */
@Slf4j
@RestControllerAdvice(annotations = RestController.class)
public class GlobalExceptionHandler {

    // ---------- Fallos de negocio (jerarquía BusinessException) ----------

    @ExceptionHandler(RecursoNoEncontradoException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNoEncontrado(RecursoNoEncontradoException ex) {
        log.warn("Recurso no encontrado: {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.NOT_FOUND.value(), ex.getMessage());
    }

    @ExceptionHandler({
            RecursoDuplicadoException.class,
            UsuarioYaAsignadoAlRetoException.class
    })
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleConflicto(BusinessException ex) {
        log.warn("Conflicto: {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.CONFLICT.value(), ex.getMessage());
    }

    @ExceptionHandler(UsuarioNoPerteneceAlGrupoException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ErrorResponse handleProhibido(UsuarioNoPerteneceAlGrupoException ex) {
        log.warn("Operación no permitida: {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.FORBIDDEN.value(), ex.getMessage());
    }

    /** Resto de hijas de BusinessException (y las futuras): 422 por defecto. */
    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ErrorResponse handleReglaNegocio(BusinessException ex) {
        log.warn("Regla de negocio incumplida: {}", ex.getMessage());
        return new ErrorResponse(HttpStatus.UNPROCESSABLE_ENTITY.value(), ex.getMessage());
    }

    // ---------- Petición mal formada (400) ----------

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleValidacion(MethodArgumentNotValidException ex) {
        String mensaje = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        return new ErrorResponse(HttpStatus.BAD_REQUEST.value(), mensaje);
    }

    @ExceptionHandler({
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class
    })
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleParametrosInvalidos(Exception ex) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "Petición inválida: revisa los parámetros de entrada");
    }

    /** JSON roto o valor que no existe en un enum (por ejemplo, un EstadoTarea desconocido). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleCuerpoIlegible(HttpMessageNotReadableException ex) {
        return new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                "El cuerpo de la petición no es válido o contiene un valor desconocido");
    }

    // ---------- Base de datos ----------

    /** Última defensa ante dos peticiones simultáneas que chocan con una UNIQUE. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleIntegridad(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad: {}", ex.getMostSpecificCause().getMessage());
        return new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                "La operación entra en conflicto con datos existentes");
    }

    // ---------- Cualquier otro fallo ----------

    @ExceptionHandler(RuntimeException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleRuntime(RuntimeException ex) {
        log.error("Error inesperado", ex);
        return new ErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Error interno del servidor");
    }
}