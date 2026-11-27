package org.zabalburu.lauki.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zabalburu.lauki.domain.Curso;
import org.zabalburu.lauki.domain.Fase;
import org.zabalburu.lauki.domain.Grupo;
import org.zabalburu.lauki.domain.MiembroGrupo;
import org.zabalburu.lauki.domain.Reto;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.domain.enums.EstadoReto;
import org.zabalburu.lauki.domain.enums.RolEnGrupo;
import org.zabalburu.lauki.exception.RecursoDuplicadoException;
import org.zabalburu.lauki.exception.RecursoNoEncontradoException;
import org.zabalburu.lauki.exception.RetoYaFinalizadoException;
import org.zabalburu.lauki.exception.UsuarioFueraDelCursoException;
import org.zabalburu.lauki.exception.UsuarioNoPerteneceAlGrupoException;
import org.zabalburu.lauki.exception.UsuarioYaAsignadoAlRetoException;
import org.zabalburu.lauki.repository.CursoRepository;
import org.zabalburu.lauki.repository.FaseRepository;
import org.zabalburu.lauki.repository.GrupoRepository;
import org.zabalburu.lauki.repository.MiembroGrupoRepository;
import org.zabalburu.lauki.repository.RetoRepository;
import org.zabalburu.lauki.repository.UsuarioRepository;

@Service
public class AdminGrupoRetoService {
	private final RetoRepository retoRepository;
	private final FaseRepository faseRepository;
	private final CursoRepository cursoRepository;
	private final UsuarioRepository usuarioRepository;
	private final GrupoRepository grupoRepository;
	private final MiembroGrupoRepository miembroGrupoRepository;
	
	@Autowired
	public AdminGrupoRetoService(
		RetoRepository retoRepository,
		FaseRepository faseRepository,
		CursoRepository cursoRepository,
		UsuarioRepository usuarioRepository,
		GrupoRepository grupoRepository,
		MiembroGrupoRepository miembroGrupoRepository) {
		
		this.retoRepository = retoRepository;
		this.faseRepository = faseRepository;
		this.cursoRepository = cursoRepository;
		this.usuarioRepository = usuarioRepository;
		this.grupoRepository = grupoRepository;
		this.miembroGrupoRepository = miembroGrupoRepository;
	}
	
	@Transactional(readOnly = true)
	public List<Reto> listarRetosPorCurso(Integer idCurso){
		List<Reto> retos = retoRepository.findAllByCurso_Id(idCurso);
		return retos;
	}
	
	@Transactional(readOnly = true)
	public List<Reto> listarTodosLosRetos() {
		List<Reto> retos = retoRepository.findAll();
		return retos;
	}
	
	@Transactional
	public Reto crearReto(Integer idCurso, String nombre, String descripcion) {
		Curso curso = cursoRepository.findById(idCurso)
				.orElseThrow(() -> new RecursoNoEncontradoException(Curso.class, idCurso));

		String NombreLimpio = nombre.trim();
		if(retoRepository.existsByNombreAndCurso_Id(NombreLimpio, idCurso)) {
			throw new RecursoDuplicadoException(Reto.class, "nombre", NombreLimpio);
		}
		
		Reto nuevoReto = new Reto();
		nuevoReto.setCurso(curso);
		nuevoReto.setNombre(NombreLimpio);
		nuevoReto.setDescripcion(descripcion);
		nuevoReto.setEstadoReto(EstadoReto.ACTIVO);
		
		return retoRepository.save(nuevoReto);
	}
	
	@Transactional
	public Grupo crearGrupo(Integer idReto, String nombre) {
	    Reto reto = retoRepository.findById(idReto)
	            .orElseThrow(() -> new RecursoNoEncontradoException(Reto.class, idReto));

	    String nombreLimpio = nombre.trim();
	    if (grupoRepository.existsByNombreAndReto_Id(nombreLimpio, idReto)) {
	        throw new RecursoDuplicadoException(Grupo.class, "nombre", nombreLimpio);
	    }

	    Grupo nuevoGrupo = new Grupo();
	    nuevoGrupo.setReto(reto);
	    nuevoGrupo.setNombre(nombreLimpio);
	    nuevoGrupo.setFechaCreacion(LocalDateTime.now());

	    Grupo grupoGuardado = grupoRepository.save(nuevoGrupo);

	    List<String> nombresFases = List.of(
	            "Análisis y Diseño",
	            "Desarrollo",
	            "Pruebas y Despliegue"
	    );

	    List<Fase> fasesBase = new ArrayList<>();
	    for (int i = 0; i < nombresFases.size(); i++) {
	        Fase fase = new Fase();
	        fase.setGrupo(grupoGuardado);
	        fase.setNombreFase(nombresFases.get(i));
	        fase.setOrden(i + 1); 
	        fasesBase.add(fase);
	    }

	    faseRepository.saveAll(fasesBase);

	    return grupoGuardado;
	}
	
	@Transactional
	public MiembroGrupo asignarMiembroAGrupo(Integer idGrupo, Integer idUsuario) {
	    Grupo grupo = grupoRepository.findById(idGrupo)
	            .orElseThrow(() -> new RecursoNoEncontradoException(Grupo.class, idGrupo));

	    Usuario usuario = usuarioRepository.findById(idUsuario)
	            .orElseThrow(() -> new RecursoNoEncontradoException(Usuario.class, idUsuario));

	    Reto reto = grupo.getReto();

	    if (reto.getEstadoReto() == EstadoReto.FINALIZADO) {
	        throw new RetoYaFinalizadoException(reto.getId());
	    }

	    if (usuario.getCurso() == null || !usuario.getCurso().getId().equals(reto.getCurso().getId())) {
	        throw new UsuarioFueraDelCursoException(idUsuario, reto.getId());
	    }

	    if (miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(idUsuario, idGrupo)) {
	        throw new RecursoDuplicadoException(MiembroGrupo.class, "usuario", idUsuario);
	    }

	    miembroGrupoRepository.findFirstByUsuario_IdAndGrupo_Reto_EstadoReto(idUsuario, EstadoReto.ACTIVO)
	            .ifPresent(miembroActivo -> {
	                throw new UsuarioYaAsignadoAlRetoException(
	                        idUsuario,
	                        miembroActivo.getGrupo().getReto().getId()
	                );
	            });

	    MiembroGrupo nuevoMiembro = new MiembroGrupo();
	    nuevoMiembro.setGrupo(grupo);
	    nuevoMiembro.setUsuario(usuario);
	    nuevoMiembro.setRolEnGrupo(RolEnGrupo.COLABORADOR);

	    return miembroGrupoRepository.save(nuevoMiembro);
	}
	
	@Transactional
	public void eliminarMiembroDeGrupo(Integer idGrupo, Integer idUsuario) {
	    Grupo grupo = grupoRepository.findById(idGrupo)
	            .orElseThrow(() -> new RecursoNoEncontradoException(Grupo.class, idGrupo));

	    if (grupo.getReto().getEstadoReto() == EstadoReto.FINALIZADO) {
	        throw new RetoYaFinalizadoException(grupo.getReto().getId());
	    }

	    if (!usuarioRepository.existsById(idUsuario)) {
	        throw new RecursoNoEncontradoException(Usuario.class, idUsuario);
	    }

	    MiembroGrupo miembro = miembroGrupoRepository.findByUsuario_IdAndGrupo_Id(idUsuario, idGrupo)
	            .orElseThrow(() -> new UsuarioNoPerteneceAlGrupoException(idUsuario, idGrupo));

	    miembroGrupoRepository.delete(miembro);
	}

	@Transactional
	public Reto finalizarReto(Integer idReto) {
	    Reto reto = retoRepository.findById(idReto)
	            .orElseThrow(() -> new RecursoNoEncontradoException(Reto.class, idReto));

	    if (reto.getEstadoReto() == EstadoReto.FINALIZADO) {
	        throw new RetoYaFinalizadoException(idReto);
	    }

	    reto.setEstadoReto(EstadoReto.FINALIZADO);
	    return retoRepository.save(reto);
	}
	
	@Transactional(readOnly = true)
	public List<Grupo> listarTodosLosGrupos() {
	    return grupoRepository.findAll();
	}

	@Transactional(readOnly = true)
	public List<MiembroGrupo> listarMiembrosPorGrupo(Integer idGrupo) {
	    if (!grupoRepository.existsById(idGrupo)) {
	        throw new RecursoNoEncontradoException(Grupo.class, idGrupo);
	    }
	    return miembroGrupoRepository.findAllByGrupo_Id(idGrupo);
	}
	
}
