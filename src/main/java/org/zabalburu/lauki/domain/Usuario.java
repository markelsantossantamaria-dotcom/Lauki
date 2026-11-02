package org.zabalburu.lauki.domain;

import org.zabalburu.lauki.domain.enums.RolSistema;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_curso", nullable = true)
	private Curso curso;
	
	@Column(nullable = false, length = 100)
	private String nombre;
	
	@Column(nullable = false, length = 150, unique = true)
	private String correo;
	
	@Column(nullable = false, length = 255)
	private String contrasena;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "rol_sistema", nullable = false)
	private RolSistema rolSistema;
	
	@Column(nullable = false)
	private boolean activo = true;
	
	
	
}
