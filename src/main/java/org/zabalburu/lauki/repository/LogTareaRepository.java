package org.zabalburu.lauki.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.LogTarea;
import org.zabalburu.lauki.domain.enums.TipoEventoLog;

@Repository
public interface LogTareaRepository extends JpaRepository<LogTarea, Integer> {

    // 1. Historial cronológico de tarea (ordenado descendentemente por FechaRegistro)
    List<LogTarea> findAllByTarea_IdOrderByFechaRegistroDesc(Integer idTarea);

    // 2. Actividad de un usuario
    List<LogTarea> findAllByUsuario_Id(Integer idUsuario);

    // 3. Filtro de tarea por evento
    List<LogTarea> findAllByTarea_IdAndTipoEventoLog(Integer idTarea, TipoEventoLog tipoEventoLog);

}