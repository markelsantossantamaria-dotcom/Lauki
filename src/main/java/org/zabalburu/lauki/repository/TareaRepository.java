package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.Tarea;
import org.zabalburu.lauki.domain.enums.EstadoTarea;

@Repository
public interface TareaRepository extends JpaRepository<Tarea, Integer> {

    List<Tarea> findAllByFase_Id(Integer idFase);

    List<Tarea> findAllByFase_IdAndEstadoTarea(Integer idFase, EstadoTarea estadoTarea);

    List<Tarea> findAllByUsuario_Id(Integer idUsuario);

    List<Tarea> findAllByUsuario_IdAndEstadoTarea(Integer idUsuario, EstadoTarea estadoTarea);

    Optional<Tarea> findByIdAndFase_Id(Integer id, Integer idFase);
    
    boolean existsByFase_Id(Integer idFase);

    List<Tarea> findAllByFase_Grupo_Id(Integer idGrupo);
}