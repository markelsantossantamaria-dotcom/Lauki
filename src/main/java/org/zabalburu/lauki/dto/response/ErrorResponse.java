package org.zabalburu.lauki.dto.response;

/**
 * Cuerpo de todas las respuestas de error de la API.
 * codigo: el mismo status HTTP de la respuesta.
 * mensaje: texto que mostrará kanban.js al alumno.
 */
public record ErrorResponse(
		int codigo, 
		String mensaje) {
}