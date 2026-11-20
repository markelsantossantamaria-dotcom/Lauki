package org.zabalburu.lauki.dto.request;

import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearTareaRequest(

		@NotBlank(message = "El título es obligatorio")
		@Size(max = 200, message = "El título no puede superar los 200 caracteres")
		String titulo,

		String descripcion,

		@NotNull(message = "La fase es obligatoria")
		Integer faseId,

		Integer responsableId,

		@DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
		LocalDateTime fechaFinalizacionEsperada
) {}