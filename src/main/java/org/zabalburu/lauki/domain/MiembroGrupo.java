package org.zabalburu.lauki.domain;

import org.zabalburu.lauki.domain.enums.RolEnGrupo;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "miembros_grupos",
	uniqueConstraints = {
        @UniqueConstraint(columnNames = {"id_grupo", "id_usuario"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MiembroGrupo {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@EqualsAndHashCode.Include
	private Integer id;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_grupo", nullable = false)
	private Grupo grupo;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "id_usuario", nullable = false)
	private Usuario usuario;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "rol_en_grupo", nullable = true, length = 20)
	private RolEnGrupo rolEnGrupo;
	
	

}
