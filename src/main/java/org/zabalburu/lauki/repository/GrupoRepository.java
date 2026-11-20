package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.Grupo;

@Repository
public interface GrupoRepository extends JpaRepository<Grupo, Integer> {

	List<Grupo> findAllByReto_Id(Integer idReto);
	
	boolean existsByNombreAndReto_Id(String nombre, Integer idReto);
	
	Optional<Grupo> findByIdAndReto_Id(Integer id, Integer idReto);
	
}
