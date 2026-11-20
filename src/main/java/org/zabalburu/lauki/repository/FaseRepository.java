package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.Fase;

@Repository
public interface FaseRepository extends JpaRepository<Fase, Integer> {

	List<Fase> findAllByGrupo_IdOrderByOrdenAsc(Integer idGrupo);
	
	boolean existsByNombreFaseAndGrupo_Id(String nombreFase, Integer idGrupo);
	
	Optional<Fase> findByIdAndGrupo_Id(Integer id, Integer idGrupo);
	
	
}
