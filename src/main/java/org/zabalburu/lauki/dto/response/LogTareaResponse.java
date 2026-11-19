package org.zabalburu.lauki.dto.response;

import java.time.LocalDateTime;

import org.zabalburu.lauki.domain.enums.EstadoTarea;
import org.zabalburu.lauki.domain.enums.TipoEventoLog;

public record LogTareaResponse(
		Integer id,
		TipoEventoLog tipoEvento,
		EstadoTarea estadoNuevo,
		String comentario,
		LocalDateTime fechaRegistro,
		Integer responsableId,
		String responsableNombre
		) {}
