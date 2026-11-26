package org.zabalburu.lauki.domain;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.zabalburu.lauki.domain.enums.EstadoTarea;

import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
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
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class Tarea {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_fase", nullable = false)
	private Fase fase;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_asignado", nullable = true)
	private Usuario usuario;
	
	@Column(nullable = false, length = 200)
	private String titulo;
	
	@Column(columnDefinition = "TEXT", nullable = true)
	private String descripcion;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_tarea", nullable = false, length = 25)
	private EstadoTarea estadoTarea = EstadoTarea.PENDIENTE;
	
	@Column(name = "fecha_creacion_real", nullable = false, updatable = false)
	private LocalDateTime fechaCreacionReal = LocalDateTime.now();
	
	@Column(name = "fecha_inicializacion", nullable = true)
	private LocalDateTime fechaInicializacion;
	
	@Column(name = "fecha_finalizacion_esperada", nullable = true)
	private LocalDateTime fechaFinalizacionEsperada;
	
	@Column(name = "fecha_finalizacion_real", nullable = true)
	private LocalDateTime fechaFinalizacionReal;
	
	@OneToMany(mappedBy = "tarea", cascade = CascadeType.ALL, orphanRemoval = true)
	@OrderBy("fechaRegistro DESC")
	private List<LogTarea> logs = new ArrayList<>();
	
	public void agregarLog(LogTarea log) {
		logs.add(log);
		log.setTarea(this);
	}
	
}
