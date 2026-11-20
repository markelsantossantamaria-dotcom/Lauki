package org.zabalburu.lauki.exception;

import java.net.URI;

import org.springframework.http.HttpHeaders;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.zabalburu.lauki.controller.TableroController;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

/**
 * Traduce las excepciones de TableroController a una redirección con el aviso
 * en el atributo flash "toastError". GlobalExceptionHandler responde en JSON
 * para los controladores REST.
 * Spring elige siempre el handler cuyo tipo está más cerca en la herencia.
 */
@Slf4j
@ControllerAdvice(assignableTypes = TableroController.class)
public class MvcExceptionHandler {

	private static final String RUTA_TABLERO = "/estudiante/tablero";

	private static final String MSG_DATOS_INVALIDOS = "Los datos enviados no son válidos";
	private static final String MSG_CAMPO_INVALIDO = "El valor del campo '%s' no es válido";
	private static final String MSG_ERROR_INESPERADO = "Ha ocurrido un error inesperado. Inténtalo de nuevo";

	// ---------- Fallos de negocio (jerarquía BusinessException) ----------

	@ExceptionHandler(BusinessException.class)
	public String handleReglaNegocio(BusinessException ex, HttpServletRequest request, RedirectAttributes redirect) {
		log.warn("Regla de negocio incumplida: {}", ex.getMessage());
		return redirigirConError(ex.getMessage(), request, redirect);
	}

	// ---------- Formulario inválido: @Valid o conversión de tipos ----------

	@ExceptionHandler(BindException.class)
	public String handleValidacion(BindException ex, HttpServletRequest request, RedirectAttributes redirect) {
		String mensaje = ex.getBindingResult().getAllErrors().stream()
				.findFirst()
				.map(error -> {
					if (error instanceof FieldError campo && campo.isBindingFailure()) {
						return MSG_CAMPO_INVALIDO.formatted(campo.getField());
					}
					return error.getDefaultMessage() != null ? error.getDefaultMessage() : MSG_DATOS_INVALIDOS;
				})
				.orElse(MSG_DATOS_INVALIDOS);
		log.warn("Formulario inválido: {}", mensaje);
		return redirigirConError(mensaje, request, redirect);
	}

	// ---------- Cualquier otro fallo ----------

	@ExceptionHandler(RuntimeException.class)
	public String handleRuntime(RuntimeException ex, HttpServletRequest request, RedirectAttributes redirect) {
		log.error("Error inesperado", ex);
		return redirigirConError(MSG_ERROR_INESPERADO, request, redirect);
	}

	// ---------- Utilidades ----------

	private String redirigirConError(String mensaje, HttpServletRequest request, RedirectAttributes redirect) {
		redirect.addFlashAttribute("toastError", mensaje);
		return "redirect:" + calcularDestino(request);
	}

	/**
	 * Vuelve a la página desde la que se envió la petición (cabecera Referer).
	 * Solo se aprovecha la ruta y la query, nunca el host, para no redirigir a sitios externos.
	 * Sin Referer válido, vuelve al tablero.
	 */
	private String calcularDestino(HttpServletRequest request) {
		String referer = request.getHeader(HttpHeaders.REFERER);
		if (referer != null) {
			try {
				URI uri = URI.create(referer);
				String ruta = uri.getRawPath();
				if (ruta != null && ruta.startsWith("/") && !ruta.startsWith("//") && !ruta.startsWith("/\\")) {
					return uri.getRawQuery() != null ? ruta + "?" + uri.getRawQuery() : ruta;
				}
			} catch (IllegalArgumentException e) {
				log.debug("Cabecera Referer no válida: {}", referer);
			}
		}
		return RUTA_TABLERO;
	}
}
