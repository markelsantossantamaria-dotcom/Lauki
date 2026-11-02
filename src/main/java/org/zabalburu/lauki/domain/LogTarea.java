package org.zabalburu.lauki.domain;

import java.time.LocalDateTime;

import org.zabalburu.lauki.domain.enums.EstadoTarea;
import org.zabalburu.lauki.domain.enums.TipoEventoLog;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "log_tareas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LogTarea {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_tarea", nullable = false)
	private Tarea tarea;
	
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tipo_evento", nullable = false)
	private TipoEventoLog tipoEventoLog;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_anterior", nullable = true)
	private EstadoTarea estadoAnterior;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "estado_nuevo", nullable = true)
	private EstadoTarea estadoNuevo;
	
	@Column(columnDefinition = "TEXT", nullable = true)
	private String comentario;
	
	@Column(name = "fecha_registro", nullable = false)
	private LocalDateTime fechaRegistro;
}
