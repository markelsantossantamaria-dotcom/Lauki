package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.Curso;


@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

	//Consulta para buscar cursos activos e inactivos
	List<Curso> findAllByActivo(boolean activo);

	//Consulta para buscar curso por nombre
	Optional<Curso> findByNombreContainingIgnoreCase(String nombre);
	
}
