package org.zabalburu.lauki.dto.request;

import org.zabalburu.lauki.domain.enums.EstadoTarea;

import jakarta.validation.constraints.NotNull;

public record CambiarEstadoTareaRequest(
		@NotNull(message = "El estado es obligatorio")
		EstadoTarea estado
) {}
