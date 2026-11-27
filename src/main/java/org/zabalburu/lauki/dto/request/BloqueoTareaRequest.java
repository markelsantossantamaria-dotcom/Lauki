package org.zabalburu.lauki.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record BloqueoTareaRequest(
		@NotNull(message = "El identificador de la tarea es obligatorio")
		Integer id,

		@NotBlank(message = "El motivo del bloqueo es obligatorio")
		String motivoBloqueo
) {}
