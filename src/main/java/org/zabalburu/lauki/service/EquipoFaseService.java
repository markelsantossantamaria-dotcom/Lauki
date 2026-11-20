package org.zabalburu.lauki.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zabalburu.lauki.domain.Fase;
import org.zabalburu.lauki.domain.Grupo;
import org.zabalburu.lauki.domain.MiembroGrupo;
import org.zabalburu.lauki.domain.Reto;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.domain.enums.EstadoReto;
import org.zabalburu.lauki.domain.enums.RolEnGrupo;
import org.zabalburu.lauki.exception.ReglaNegocioException;
import org.zabalburu.lauki.exception.RecursoNoEncontradoException;
import org.zabalburu.lauki.exception.RetoNoEditableException;
import org.zabalburu.lauki.exception.UsuarioNoPerteneceAlGrupoException;
import org.zabalburu.lauki.mapper.FaseMapper;
import org.zabalburu.lauki.repository.FaseRepository;
import org.zabalburu.lauki.repository.MiembroGrupoRepository;
import org.zabalburu.lauki.repository.TareaRepository;
import org.zabalburu.lauki.repository.UsuarioRepository;

@Service
public class EquipoFaseService {

    private final MiembroGrupoRepository miembroGrupoRepository;
    private final FaseRepository faseRepository;
    private final TareaRepository tareaRepository;
    private final UsuarioRepository usuarioRepository;
    private final FaseMapper faseMapper;

    @Autowired
    public EquipoFaseService(
            MiembroGrupoRepository miembroGrupoRepository,
            FaseRepository faseRepository,
            TareaRepository tareaRepository,
            UsuarioRepository usuarioRepository,
            FaseMapper faseMapper) {
        this.miembroGrupoRepository = miembroGrupoRepository;
        this.faseRepository = faseRepository;
        this.tareaRepository = tareaRepository;
        this.usuarioRepository = usuarioRepository;
        this.faseMapper = faseMapper;
    }

    public record MiembroEquipoDTO(
            Integer usuarioId,
            String nombreCompleto,
            String inicial,
            RolEnGrupo rol) {
    }

    @Transactional(readOnly = true)
    public MiembroGrupo obtenerGrupoActivo(Integer idUsuario) {
        return miembroGrupoRepository
                .findFirstByUsuario_IdAndGrupo_Reto_EstadoReto(idUsuario, EstadoReto.ACTIVO)
                .orElseThrow(() -> new RecursoNoEncontradoException(Grupo.class, idUsuario));
    }

    @Transactional(readOnly = true)
    public MiembroGrupo obtenerGrupoActivo(String correo) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);
        return obtenerGrupoActivo(usuario.getId());
    }

    @Transactional(readOnly = true)
    public List<MiembroEquipoDTO> listarMiembros(Integer idUsuario) {
        MiembroGrupo miembroActivo = obtenerGrupoActivo(idUsuario);
        Integer idGrupo = miembroActivo.getGrupo().getId();

        return miembroGrupoRepository.findAllByGrupo_Id(idGrupo).stream()
                .map(this::toMiembroEquipoDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MiembroEquipoDTO> listarMiembros(String correo) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);
        return listarMiembros(usuario.getId());
    }

    @Transactional
    public void asignarRol(Integer idUsuarioActor, Integer idUsuarioObjetivo, RolEnGrupo rol) {
        if (rol == null) {
            throw new ReglaNegocioException("El rol no puede estar vacío");
        }

        MiembroGrupo miembroActor = obtenerGrupoActivo(idUsuarioActor);
        Grupo grupo = miembroActor.getGrupo();
        validarRetoActivo(grupo);

        if (!miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(idUsuarioActor, grupo.getId())) {
            throw new UsuarioNoPerteneceAlGrupoException(idUsuarioActor, grupo.getId());
        }

        MiembroGrupo miembroObjetivo = miembroGrupoRepository
                .findByUsuario_IdAndGrupo_Id(idUsuarioObjetivo, grupo.getId())
                .orElseThrow(() -> new UsuarioNoPerteneceAlGrupoException(idUsuarioObjetivo, grupo.getId()));

        miembroObjetivo.setRolEnGrupo(rol);
        miembroGrupoRepository.save(miembroObjetivo);
    }

    @Transactional
    public void asignarRol(String correoActor, Integer idUsuarioObjetivo, RolEnGrupo rol) {
        Usuario actor = buscarUsuarioPorCorreo(correoActor);
        asignarRol(actor.getId(), idUsuarioObjetivo, rol);
    }

    @Transactional
    public void asignarRol(String correoActor, Integer idUsuarioObjetivo, String rol) {
        RolEnGrupo rolEnGrupo;
        try {
            rolEnGrupo = RolEnGrupo.valueOf(rol == null ? "" : rol.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new ReglaNegocioException("El rol indicado no es válido");
        }
        asignarRol(correoActor, idUsuarioObjetivo, rolEnGrupo);
    }

    @Transactional(readOnly = true)
    public List<FaseMapper.FaseDTO> listarFases(Integer idUsuario) {
        MiembroGrupo miembroActivo = obtenerGrupoActivo(idUsuario);
        return faseRepository.findAllByGrupo_IdOrderByOrdenAsc(miembroActivo.getGrupo().getId())
                .stream()
                .map(faseMapper::toDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<FaseMapper.FaseDTO> listarFases(String correo) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);
        return listarFases(usuario.getId());
    }

    @Transactional
    public Fase crearFase(Integer idUsuario, String nombre, Integer orden) {
        if (nombre == null || nombre.trim().isEmpty()) {
            throw new ReglaNegocioException("El nombre de la fase no puede estar vacío");
        }
        if (orden == null || orden < 1) {
            throw new ReglaNegocioException("El orden de la fase debe ser mayor o igual que 1");
        }

        MiembroGrupo miembroActivo = obtenerGrupoActivo(idUsuario);
        Grupo grupo = miembroActivo.getGrupo();
        validarRetoActivo(grupo);

        if (!miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(idUsuario, grupo.getId())) {
            throw new UsuarioNoPerteneceAlGrupoException(idUsuario, grupo.getId());
        }

        String nombreNormalizado = nombre.trim();
        if (faseRepository.existsByNombreFaseAndGrupo_Id(nombreNormalizado, grupo.getId())) {
            throw new ReglaNegocioException("Ya existe una fase con el nombre " + nombreNormalizado);
        }

        Fase fase = new Fase();
        fase.setGrupo(grupo);
        fase.setNombreFase(nombreNormalizado);
        fase.setOrden(orden);
        return faseRepository.save(fase);
    }

    @Transactional
    public Fase crearFase(String correo, String nombre, Integer orden) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);
        return crearFase(usuario.getId(), nombre, orden);
    }

    @Transactional
    public void eliminarFase(Integer idUsuario, Integer idFase) {
        MiembroGrupo miembroActivo = obtenerGrupoActivo(idUsuario);
        Grupo grupo = miembroActivo.getGrupo();
        validarRetoActivo(grupo);

        if (!miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(idUsuario, grupo.getId())) {
            throw new UsuarioNoPerteneceAlGrupoException(idUsuario, grupo.getId());
        }

        Fase fase = faseRepository.findByIdAndGrupo_Id(idFase, grupo.getId())
                .orElseThrow(() -> new RecursoNoEncontradoException(Fase.class, idFase));

        if (tareaRepository.existsByFase_Id(fase.getId())) {
            throw new ReglaNegocioException("La fase " + idFase + " tiene tareas y no puede eliminarse");
        }

        faseRepository.delete(fase);
    }

    @Transactional
    public void eliminarFase(String correo, Integer idFase) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);
        eliminarFase(usuario.getId(), idFase);
    }

    @Transactional(readOnly = true)
    public List<MiembroGrupo> listarHistorial(Integer idUsuario) {
        return miembroGrupoRepository.findAllByUsuario_Id(idUsuario).stream()
                .filter(miembro -> Optional.ofNullable(miembro)
                        .map(MiembroGrupo::getGrupo)
                        .map(Grupo::getReto)
                        .map(Reto::getEstadoReto)
                        .filter(estado -> estado == EstadoReto.FINALIZADO)
                        .isPresent())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MiembroGrupo> listarHistorial(String correo) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);
        return listarHistorial(usuario.getId());
    }

    @Transactional(readOnly = true)
    public Grupo cargarHistorial(Integer idUsuario, Integer idGrupo) {
        MiembroGrupo participacion = miembroGrupoRepository
                .findByUsuario_IdAndGrupo_Id(idUsuario, idGrupo)
                .orElseThrow(() -> new UsuarioNoPerteneceAlGrupoException(idUsuario, idGrupo));

        Reto reto = participacion.getGrupo().getReto();
        if (reto == null || reto.getEstadoReto() != EstadoReto.FINALIZADO) {
            EstadoReto estado = reto == null ? EstadoReto.INACTIVO : reto.getEstadoReto();
            throw new RetoNoEditableException(reto == null ? idGrupo : reto.getId(), estado);
        }
        return participacion.getGrupo();
    }

    @Transactional(readOnly = true)
    public Grupo cargarHistorial(String correo, Integer idGrupo) {
        Usuario usuario = buscarUsuarioPorCorreo(correo);
        return cargarHistorial(usuario.getId(), idGrupo);
    }

    private Usuario buscarUsuarioPorCorreo(String correo) {
        return Optional.ofNullable(correo)
                .map(String::trim)
                .filter(correoNormalizado -> !correoNormalizado.isEmpty())
                .flatMap(usuarioRepository::findByCorreoIgnoreCase)
                .orElseThrow(() -> new RecursoNoEncontradoException(Usuario.class, null));
    }

    private MiembroEquipoDTO toMiembroEquipoDTO(MiembroGrupo miembro) {
        Optional<Usuario> usuario = Optional.ofNullable(miembro)
                .map(MiembroGrupo::getUsuario);
        String nombre = usuario
                .map(Usuario::getNombre)
                .map(String::trim)
                .filter(nombreNormalizado -> !nombreNormalizado.isEmpty())
                .orElse("");
        String inicial = Optional.of(nombre)
                .filter(nombreNormalizado -> !nombreNormalizado.isEmpty())
                .map(nombreNormalizado -> nombreNormalizado.substring(0, 1).toUpperCase())
                .orElse("");
        RolEnGrupo rol = Optional.ofNullable(miembro)
                .map(MiembroGrupo::getRolEnGrupo)
                .orElse(RolEnGrupo.COLABORADOR);

        return new MiembroEquipoDTO(
                usuario.map(Usuario::getId).orElse(null),
                nombre,
                inicial,
                rol);
    }

    private void validarRetoActivo(Grupo grupo) {
        Optional<Reto> reto = Optional.ofNullable(grupo).map(Grupo::getReto);
        EstadoReto estado = reto
                .map(Reto::getEstadoReto)
                .orElse(EstadoReto.INACTIVO);
        if (reto.isEmpty() || estado != EstadoReto.ACTIVO) {
            Integer idRecurso = reto.map(Reto::getId)
                    .orElseGet(() -> Optional.ofNullable(grupo)
                            .map(Grupo::getId)
                            .orElse(null));
            throw new RetoNoEditableException(idRecurso, estado);
        }
    }
}
