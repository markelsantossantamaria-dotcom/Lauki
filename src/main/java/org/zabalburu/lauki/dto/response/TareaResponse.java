package org.zabalburu.lauki.dto.response;

import java.time.LocalDateTime;
import java.util.List;

import org.zabalburu.lauki.domain.enums.EstadoTarea;

public record TareaResponse(
		Integer id,
		String titulo,
		String descripcion,
		EstadoTarea estado,
		Integer faseId,
		String faseNombre,
		Integer faseOrden,
		Integer responsableId,
		String responsableInicial,
		LocalDateTime fechaCreacion,
		LocalDateTime fechaInicializacion,
		LocalDateTime fechaFinalizacionEsperada,
		LocalDateTime fechaFinalizacionReal,
		String ultimoMotivoBloqueo,
		long totalComentarios,
		List<LogTareaResponse> logs
) {}
