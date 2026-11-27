package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.MiembroGrupo;
import org.zabalburu.lauki.domain.enums.EstadoReto;

@Repository
public interface MiembroGrupoRepository extends JpaRepository<MiembroGrupo, Integer> {

	List<MiembroGrupo> findAllByGrupo_Id(Integer idGrupo);

	boolean existsByUsuario_IdAndGrupo_Id(Integer idUsuario, Integer idGrupo);

	Optional<MiembroGrupo> findByUsuario_IdAndGrupo_Id(Integer idUsuario, Integer idGrupo);

	List<MiembroGrupo> findAllByUsuario_Id(Integer idUsuario);

	Optional<MiembroGrupo> findFirstByUsuario_IdAndGrupo_Reto_EstadoReto(Integer idUsuario, EstadoReto estadoReto);

	// Listado de grupos de retos en estado FINALIZADO en los que participó el
	// estudiante y acceso en modo soloLectura = true.
	List<MiembroGrupo> findAllByUsuario_IdAndGrupo_Reto_EstadoReto(Integer idUsuario, EstadoReto estadoReto);
}
