package org.zabalburu.lauki.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cursos", uniqueConstraints = {
    @UniqueConstraint(name = "nombre_periodo", columnNames = {"nombre", "periodo_academico"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Curso {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	
	@Column(nullable = false, length = 100)
	private String nombre;
	
	@Column(name = "periodo_academico", length = 9, nullable = true)
	private String periodoAcademico;
	
	@Column(nullable = false)
	private boolean activo;
		
	
}
