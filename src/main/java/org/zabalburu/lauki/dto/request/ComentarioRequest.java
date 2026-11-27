package org.zabalburu.lauki.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ComentarioRequest(

		@NotBlank(message = "El comentario no puede estar vacío")
		String comentario
) {}

