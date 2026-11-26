package org.zabalburu.lauki.mapper;

import org.springframework.stereotype.Component;
import org.zabalburu.lauki.domain.LogTarea;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.dto.response.LogTareaResponse;

@Component
public class LogTareaMapper {

	public LogTareaResponse toDTO(LogTarea entity) {
		if (entity == null) {
			return null;
		}

		Usuario usuario = entity.getUsuario();

		return new LogTareaResponse(
				entity.getId(),
				entity.getTipoEventoLog(),
				entity.getEstadoNuevo(),
				entity.getComentario(),
				entity.getFechaRegistro(),
				usuario != null ? usuario.getId() : null,
				usuario != null ? usuario.getNombre() : null
		);
	}
}
