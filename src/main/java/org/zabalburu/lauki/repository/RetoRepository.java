package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.Reto;
import org.zabalburu.lauki.domain.enums.EstadoReto;

@Repository
public interface RetoRepository extends JpaRepository<Reto, Integer> {

    List<Reto> findAllByCurso_Id(Integer idCurso);

    List<Reto> findAllByCurso_IdAndEstadoReto(Integer idCurso, EstadoReto estadoReto);

    boolean existsByNombreAndCurso_Id(String nombre, Integer idCurso);

    Optional<Reto> findByIdAndCurso_Id(Integer id, Integer idCurso);

}