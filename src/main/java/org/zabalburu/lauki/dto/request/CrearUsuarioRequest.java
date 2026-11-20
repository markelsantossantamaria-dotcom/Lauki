package org.zabalburu.lauki.dto.request;

import org.zabalburu.lauki.domain.enums.RolSistema;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest (
	
	Integer idCurso,
	
	@NotBlank(message = "El nombre del usuario no puede estar en blanco")
	@Size(max = 100, message = "El nombre del usuario no puede exceder los 100 caracteres")
	String nombre,
	
	@NotBlank(message = "El correo del usuario no puede estar en blanco")
	@Email(message = "El formato de del correo no es valido")
	String correo,
	
	@NotBlank(message = "La contraseña del usuario no puede estar en blanco")
	@Size(max = 255, message = "La contraseña del usuario no puede exceder los 255 carcateres")
	String contrasena,
	
	@NotNull(message = "El usuario tiene que tener un rol asignado obligatoriamente")
	RolSistema rolSistema) {
	
}
