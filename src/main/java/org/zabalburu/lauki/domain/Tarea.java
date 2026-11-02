package org.zabalburu.lauki.domain;

import java.time.LocalDateTime;

import org.zabalburu.lauki.domain.enums.EstadoTarea;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "tareas")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Tarea {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Integer id;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_fase", nullable = false)
	private Fase fase;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_asignado", nullable = true)
	private Usuario usuario;
	
	@Column(nullable = false, length = 200)
	private String titulo;
	
	@Column(columnDefinition = "TEXT", nullable = true)
	private String descripcion;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_tarea", nullable = false)
	private EstadoTarea estadoTarea;
	
	@Column(name = "fecha_creacion_real", nullable = false)
	private LocalDateTime fechaCreacionReal;
	
	@Column(name = "fecha_inicializacion", nullable = true)
	private LocalDateTime fechaInicializacion;
	
	@Column(name = "fecha_finalizacion_esperada", nullable = true)
	private LocalDateTime fechaFinalizacionEsperada;
	
	@Column(name = "fecha_finalizacion_real", nullable = true)
	private LocalDateTime fechaFinalizacionReal;
	
	
}
