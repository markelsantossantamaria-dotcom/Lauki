package org.zabalburu.lauki.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zabalburu.lauki.domain.Fase;
import org.zabalburu.lauki.domain.Grupo;
import org.zabalburu.lauki.domain.LogTarea;
import org.zabalburu.lauki.domain.Reto;
import org.zabalburu.lauki.domain.Tarea;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.domain.enums.EstadoReto;
import org.zabalburu.lauki.domain.enums.EstadoTarea;
import org.zabalburu.lauki.domain.enums.RolSistema;
import org.zabalburu.lauki.domain.enums.TipoEventoLog;
import org.zabalburu.lauki.dto.request.BloqueoTareaRequest;
import org.zabalburu.lauki.dto.request.CambiarEstadoTareaRequest;
import org.zabalburu.lauki.dto.request.ComentarioRequest;
import org.zabalburu.lauki.dto.request.CrearTareaRequest;
import org.zabalburu.lauki.dto.response.TareaResponse;
import org.zabalburu.lauki.exception.RecursoInactivoException;
import org.zabalburu.lauki.exception.RecursoNoEncontradoException;
import org.zabalburu.lauki.exception.ReglaNegocioException;
import org.zabalburu.lauki.exception.ResponsableAjenoAlGrupoException;
import org.zabalburu.lauki.exception.RetoNoEditableException;
import org.zabalburu.lauki.exception.TareaSinResponsableException;
import org.zabalburu.lauki.exception.TransicionEstadoInvalidaException;
import org.zabalburu.lauki.exception.UsuarioNoPerteneceAlGrupoException;
import org.zabalburu.lauki.mapper.TareaMapper;
import org.zabalburu.lauki.repository.FaseRepository;
import org.zabalburu.lauki.repository.GrupoRepository;
import org.zabalburu.lauki.repository.MiembroGrupoRepository;
import org.zabalburu.lauki.repository.TareaRepository;
import org.zabalburu.lauki.repository.UsuarioRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class KanbanTareaService {

    private final TareaRepository tareaRepository;
    private final FaseRepository faseRepository;
    private final GrupoRepository grupoRepository;
    private final MiembroGrupoRepository miembroGrupoRepository;
    private final UsuarioRepository usuarioRepository;
    private final TareaMapper tareaMapper;

    @Transactional(readOnly = true)
    public List<TareaResponse> listarTareas(String correoActor, Integer idGrupo) {
        Usuario actor = buscarActor(correoActor);
        Grupo grupo = grupoRepository.findById(idGrupo)
                .orElseThrow(() -> new RecursoNoEncontradoException(Grupo.class, idGrupo));
        if (actor.getRolSistema() != RolSistema.ADMINISTRADOR) {
            validarEstudianteDelGrupo(actor, grupo);
        }
        validarRetoActivo(grupo);
        return tareaRepository.findAllByFase_Grupo_Id(idGrupo).stream()
                .map(tareaMapper::toDTO)
                .toList();
    }

    public TareaResponse crearTarea(String correoActor, CrearTareaRequest solicitud) {
        Usuario actor = buscarActor(correoActor);
        validarTitulo(solicitud.titulo());
        if (solicitud.faseId() == null) {
            throw new ReglaNegocioException("La fase es obligatoria");
        }
        Fase fase = faseRepository.findById(solicitud.faseId())
                .orElseThrow(() -> new RecursoNoEncontradoException(Fase.class, solicitud.faseId()));
        Grupo grupo = fase.getGrupo();
        validarEscritura(actor, grupo);

        Usuario responsable = buscarResponsable(solicitud.responsableId(), grupo);
        Tarea tarea = tareaMapper.toEntity(solicitud, fase, responsable);
        registrarEvento(tarea, actor, TipoEventoLog.CREACION, null, EstadoTarea.PENDIENTE, null);
        return tareaMapper.toDTO(tareaRepository.saveAndFlush(tarea));
    }

    public TareaResponse editarTarea(String correoActor, Integer idTarea, String titulo,
            Integer faseId, Integer responsableId) {
        Usuario actor = buscarActor(correoActor);
        Tarea tarea = buscarTareaEditable(actor, idTarea);
        validarTitulo(titulo);
        if (faseId == null) {
            throw new ReglaNegocioException("La fase es obligatoria");
        }
        Grupo grupo = tarea.getFase().getGrupo();
        Fase fase = faseRepository.findByIdAndGrupo_Id(faseId, grupo.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(Fase.class, faseId));

        actualizarResponsable(tarea, responsableId);
        tarea.setTitulo(titulo);
        tarea.setFase(fase);
        return tareaMapper.toDTO(tarea);
    }

    public TareaResponse asignarResponsable(String correoActor, Integer idTarea, Integer responsableId) {
        Tarea tarea = buscarTareaEditable(buscarActor(correoActor), idTarea);
        actualizarResponsable(tarea, responsableId);
        return tareaMapper.toDTO(tarea);
    }

    public TareaResponse actualizarDescripcion(String correoActor, Integer idTarea, String descripcion) {
        Tarea tarea = buscarTareaEditable(buscarActor(correoActor), idTarea);
        tarea.setDescripcion(descripcion);
        return tareaMapper.toDTO(tarea);
    }

    public TareaResponse cambiarEstado(String correoActor, Integer idTarea, CambiarEstadoTareaRequest solicitud) {
        Usuario actor = buscarActor(correoActor);
        Tarea tarea = buscarTareaEditable(actor, idTarea);
        EstadoTarea destino = solicitud.estado();
        validarResponsableAsignado(tarea);
        if (destino == null) {
            throw new ReglaNegocioException("El estado es obligatorio");
        }
        if (destino == EstadoTarea.BLOQUEADA) {
            throw TransicionEstadoInvalidaException.bloqueoSinMotivo();
        }
        if (destino == tarea.getEstadoTarea()) {
            throw TransicionEstadoInvalidaException.mismoEstado(destino);
        }
        aplicarTransicion(tarea, actor, destino, TipoEventoLog.TRANSICION, null);
        tareaRepository.flush();
        return tareaMapper.toDTO(tarea);
    }

    public TareaResponse bloquear(String correoActor, BloqueoTareaRequest solicitud) {
        Usuario actor = buscarActor(correoActor);
        Tarea tarea = buscarTareaEditable(actor, solicitud.id());
        validarResponsableAsignado(tarea);
        if (tarea.getEstadoTarea() == EstadoTarea.BLOQUEADA) {
            throw TransicionEstadoInvalidaException.yaBloqueada();
        }
        String motivo = solicitud.motivoBloqueo();
        if (motivo == null || motivo.isBlank()) {
            throw TransicionEstadoInvalidaException.bloqueoSinMotivo();
        }
        aplicarTransicion(tarea, actor, EstadoTarea.BLOQUEADA, TipoEventoLog.BLOQUEO, motivo.strip());
        tareaRepository.flush();
        return tareaMapper.toDTO(tarea);
    }

    public TareaResponse comentar(String correoActor, Integer idTarea, ComentarioRequest solicitud) {
        Usuario actor = buscarActor(correoActor);
        Tarea tarea = buscarTareaEditable(actor, idTarea);
        String comentario = solicitud.comentario();
        if (comentario == null || comentario.isBlank()) {
            throw new ReglaNegocioException("El comentario no puede estar vacío");
        }
        registrarEvento(tarea, actor, TipoEventoLog.COMENTARIO, null, null, comentario.strip());
        tareaRepository.flush();
        return tareaMapper.toDTO(tarea);
    }

    public void eliminarTarea(String correoActor, Integer idTarea) {
        Tarea tarea = buscarTareaEditable(buscarActor(correoActor), idTarea);
        tareaRepository.delete(tarea);
    }

    private Usuario buscarActor(String correo) {
        Usuario actor = usuarioRepository.findByCorreoIgnoreCase(correo)
                .orElseThrow(() -> new RecursoNoEncontradoException(Usuario.class, null));
        if (!actor.isActivo()) {
            throw new RecursoInactivoException(Usuario.class, actor.getId());
        }
        return actor;
    }

    private Tarea buscarTareaEditable(Usuario actor, Integer idTarea) {
        if (idTarea == null) {
            throw new ReglaNegocioException("El identificador de la tarea es obligatorio");
        }
        Tarea tarea = tareaRepository.findById(idTarea)
                .orElseThrow(() -> new RecursoNoEncontradoException(Tarea.class, idTarea));
        validarEscritura(actor, tarea.getFase().getGrupo());
        return tarea;
    }

    private void validarEscritura(Usuario actor, Grupo grupo) {
        validarEstudianteDelGrupo(actor, grupo);
        validarRetoActivo(grupo);
    }

    private void validarEstudianteDelGrupo(Usuario actor, Grupo grupo) {
        if (actor.getRolSistema() != RolSistema.ESTUDIANTE
                || !miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(actor.getId(), grupo.getId())) {
            throw new UsuarioNoPerteneceAlGrupoException(actor.getId(), grupo.getId());
        }
    }

    private void validarRetoActivo(Grupo grupo) {
        Reto reto = grupo.getReto();
        if (reto.getEstadoReto() != EstadoReto.ACTIVO) {
            throw new RetoNoEditableException(reto.getId(), reto.getEstadoReto());
        }
    }

    private void validarTitulo(String titulo) {
        if (titulo == null || titulo.isBlank()) {
            throw new ReglaNegocioException("El título es obligatorio");
        }
        if (titulo.length() > 200) {
            throw new ReglaNegocioException("El título no puede superar los 200 caracteres");
        }
    }

    private void actualizarResponsable(Tarea tarea, Integer responsableId) {
        if (responsableId == null && tarea.getEstadoTarea() == EstadoTarea.COMPLETADA) {
            throw new TareaSinResponsableException(tarea.getId());
        }
        tarea.setUsuario(buscarResponsable(responsableId, tarea.getFase().getGrupo()));
    }

    private Usuario buscarResponsable(Integer responsableId, Grupo grupo) {
        if (responsableId == null) {
            return null;
        }
        Usuario responsable = usuarioRepository.findById(responsableId)
                .orElseThrow(() -> new RecursoNoEncontradoException(Usuario.class, responsableId));
        if (!miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(responsableId, grupo.getId())) {
            throw new ResponsableAjenoAlGrupoException(responsableId, grupo.getId());
        }
        if (!responsable.isActivo()) {
            throw new RecursoInactivoException(Usuario.class, responsableId);
        }
        return responsable;
    }

    private void validarResponsableAsignado(Tarea tarea) {
        if (tarea.getUsuario() == null) {
            throw new TareaSinResponsableException(tarea.getId());
        }
    }

    private void aplicarTransicion(Tarea tarea, Usuario actor, EstadoTarea destino,
            TipoEventoLog tipoEvento, String motivo) {
        EstadoTarea origen = tarea.getEstadoTarea();
        LocalDateTime ahora = LocalDateTime.now();
        if (origen == EstadoTarea.PENDIENTE && tarea.getFechaInicializacion() == null) {
            tarea.setFechaInicializacion(ahora);
        }
        if (destino == EstadoTarea.COMPLETADA) {
            tarea.setFechaFinalizacionReal(ahora);
        } else if (origen == EstadoTarea.COMPLETADA) {
            tarea.setFechaFinalizacionReal(null);
        }
        tarea.setEstadoTarea(destino);
        registrarEvento(tarea, actor, tipoEvento, origen, destino, motivo);
    }

    private void registrarEvento(Tarea tarea, Usuario actor, TipoEventoLog tipoEvento,
            EstadoTarea origen, EstadoTarea destino, String comentario) {
        LogTarea log = new LogTarea();
        log.setUsuario(actor);
        log.setTipoEventoLog(tipoEvento);
        log.setEstadoAnterior(origen);
        log.setEstadoNuevo(destino);
        log.setComentario(comentario);
        tarea.agregarLog(log);
        // @OrderBy se aplica al cargar la colección. El mapper también necesita ese orden en memoria.
        tarea.getLogs().sort(Comparator.comparing(LogTarea::getFechaRegistro).reversed());
    }
}
