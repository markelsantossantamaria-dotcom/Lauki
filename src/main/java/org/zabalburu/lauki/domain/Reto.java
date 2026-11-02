package org.zabalburu.lauki.domain;

import org.zabalburu.lauki.domain.enums.EstadoReto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "retos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Reto {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_curso", nullable = false)
	private Curso curso;
	
	@Column(nullable = false, length = 150)
	private String nombre;
	
	@Column(columnDefinition = "TEXT", nullable = false)
	private String descripcion;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_reto", nullable = false)
	private EstadoReto estadoReto;
	
	
	
}
