package org.zabalburu.lauki.mapper;

import org.springframework.stereotype.Component;
import org.zabalburu.lauki.domain.Curso;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.dto.request.CrearUsuarioRequest;

@Component
public class UsuarioMapper {

	public CrearUsuarioRequest toDTO(Usuario entity) {
		if(entity == null) {
			return null;
	}
		
	Integer idCurso = (entity.getCurso() != null) ? entity.getCurso().getId() : null;
		
	return new CrearUsuarioRequest(
			idCurso,
			entity.getNombre(),
			entity.getCorreo(),
			null,
			entity.getRolSistema());
	}
	
	public Usuario toEntity(CrearUsuarioRequest dto, Curso curso) {
		if (dto == null) {
			return null;
		}
		Usuario entity = new Usuario();
		entity.setCurso(curso);
		entity.setNombre(dto.nombre());
		entity.setCorreo(dto.correo());
		entity.setRolSistema(dto.rolSistema());
		entity.setActivo(true);
		return entity;
	}
}

