package org.zabalburu.lauki.mapper;

import java.util.List;

import org.springframework.stereotype.Component;
import org.zabalburu.lauki.domain.Fase;
import org.zabalburu.lauki.domain.LogTarea;
import org.zabalburu.lauki.domain.Tarea;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.domain.enums.TipoEventoLog;
import org.zabalburu.lauki.dto.request.CrearTareaRequest;
import org.zabalburu.lauki.dto.response.LogTareaResponse;
import org.zabalburu.lauki.dto.response.TareaResponse;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class TareaMapper {

	private final LogTareaMapper logTareaMapper;

	public TareaResponse toDTO(Tarea entity) {
		if (entity == null) {
			return null;
		}

		Fase fase = entity.getFase();
		Usuario usuario = entity.getUsuario();
		List<LogTarea> logs = entity.getLogs() != null ? entity.getLogs() : List.of();

		List<LogTareaResponse> listaLogs = logs.stream()
				.map(logTareaMapper::toDTO)
				.toList();

		String ultimoMotivoBloqueo = logs.stream()
				.filter(log -> log.getTipoEventoLog() == TipoEventoLog.BLOQUEO)
				.map(LogTarea::getComentario)
				.findFirst()
				.orElse(null);

		long totalComentarios = logs.stream()
				.filter(log -> log.getTipoEventoLog() == TipoEventoLog.COMENTARIO)
				.count();

		return new TareaResponse(
				entity.getId(),
				entity.getTitulo(),
				entity.getDescripcion(),
				entity.getEstadoTarea(),
				fase != null ? fase.getId() : null,
				fase != null ? fase.getNombreFase() : null,
				fase != null ? fase.getOrden() : null,
				usuario != null ? usuario.getId() : null,
				usuario != null ? calcularIniciales(usuario.getNombre()) : null,
				entity.getFechaCreacionReal(),
				entity.getFechaInicializacion(),
				entity.getFechaFinalizacionEsperada(),
				entity.getFechaFinalizacionReal(),
				ultimoMotivoBloqueo,
				totalComentarios,
				listaLogs
		);
	}

	public Tarea toEntity(CrearTareaRequest request, Fase fase, Usuario responsable) {
		if (request == null) {
			return null;
		}

		Tarea tarea = new Tarea();
		tarea.setTitulo(request.titulo());
		tarea.setDescripcion(request.descripcion());
		tarea.setFase(fase);
		tarea.setUsuario(responsable);
		tarea.setFechaFinalizacionEsperada(request.fechaFinalizacionEsperada());
		return tarea;
	}

	private String calcularIniciales(String nombre) {
		if (nombre == null || nombre.isBlank()) {
			return null;
		}
		String[] partes = nombre.trim().split("\\s+");
		StringBuilder iniciales = new StringBuilder();
		iniciales.append(Character.toUpperCase(partes[0].charAt(0)));
		if (partes.length > 1) {
			iniciales.append(Character.toUpperCase(partes[1].charAt(0)));
		}
		return iniciales.toString();
	}
}
