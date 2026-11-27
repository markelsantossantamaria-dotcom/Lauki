package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.Curso;


@Repository
public interface CursoRepository extends JpaRepository<Curso, Integer> {

	//Consulta para buscar cursos activos e inactivos
	List<Curso> findAllByActivo(boolean activo);

	//Consulta para buscar curso por nombre
	Optional<Curso> findByNombreContainingIgnoreCase(String nombre);
	
	List<Curso> findAllByPeriodoAcademicoAndActivoTrue(String periodoAcademico);

	@Query("SELECT DISTINCT c.periodoAcademico FROM Curso c WHERE c.periodoAcademico IS NOT NULL AND c.activo = true ORDER BY c.periodoAcademico DESC")
	List<String> findDistinctPeriodosAcademicos();

	@Query("SELECT COUNT(c) > 0 FROM Curso c WHERE REPLACE(LOWER(c.nombre), ' ', '') = REPLACE(LOWER(:nombre), ' ', '') AND c.periodoAcademico = :periodoAcademico")
	boolean existsByNombreAndPeriodoAcademico(@Param("nombre") String nombre, @Param("periodoAcademico") String periodoAcademico);

	@Query("SELECT COUNT(c) > 0 FROM Curso c WHERE REPLACE(LOWER(c.nombre), ' ', '') = REPLACE(LOWER(:nombre), ' ', '')")
    boolean existsByNombre(@Param("nombre") String nombre);
	
}
