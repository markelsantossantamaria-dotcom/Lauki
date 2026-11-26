package org.zabalburu.lauki.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.zabalburu.lauki.domain.Curso;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.dto.request.CrearUsuarioRequest;
import org.zabalburu.lauki.exception.RecursoDuplicadoException;
import org.zabalburu.lauki.exception.RecursoNoEncontradoException;
import org.zabalburu.lauki.mapper.UsuarioMapper;
import org.zabalburu.lauki.repository.CursoRepository;
import org.zabalburu.lauki.repository.UsuarioRepository;

@Service
public class AdminCursoUsuarioService {
	private final UsuarioRepository usuarioRepository;
	private final CursoRepository cursoRepository;
	private final PasswordEncoder passwordEncoder;
	private final UsuarioMapper usuarioMapper;
	
	@Autowired
	public AdminCursoUsuarioService(
			UsuarioRepository usuarioRepository,
			CursoRepository cursoRepository,
			PasswordEncoder passwEncoder,
			UsuarioMapper usuarioMapper) {
		this.usuarioRepository = usuarioRepository;
		this.cursoRepository = cursoRepository;
		this.passwordEncoder = passwEncoder;
		this.usuarioMapper = usuarioMapper;
	}

	@Transactional(readOnly = true)
	public List<Curso> listarTodosLosCursos(){
		List<Curso> cursos = cursoRepository.findAll();
		return cursos;
	}
	
	@Transactional(readOnly = true)
	public List<Curso> listarTodosLosCursosActivos(){
		List<Curso> cursos = cursoRepository.findAllByActivo(true);
		return cursos;
	}
	
	@Transactional
	public Curso crearCurso(String nombre) {
		if(cursoRepository.existsByNombre(nombre)) {
			throw new RecursoDuplicadoException(Curso.class, "nombre", nombre);
		}
		Curso nuevoCurso = new Curso();
		nuevoCurso.setNombre(nombre.trim());
		nuevoCurso.setActivo(true);
		
		Curso cursoGuardado = cursoRepository.save(nuevoCurso);
		
		return cursoGuardado;
	}
	
	@Transactional(readOnly = true)
	public List<Usuario> listarTodosLosUsuarios(){
		List<Usuario> usuarios = usuarioRepository.findAll();
		return usuarios;
	}
	
	@Transactional(readOnly = true)
	public List<Usuario> listarTodosLosUsuariosActivos(){
		List<Usuario> usuarios = usuarioRepository.findAllByActivo(true);
		return usuarios;
	}
	
	@Transactional
	public Usuario crearUsuario(CrearUsuarioRequest request) {
		if(usuarioRepository.existsByCorreo(request.correo())) {
			throw new RecursoDuplicadoException(Usuario.class, "correo", request.correo());
		}
		
		Curso curso = null;
	    if (request.idCurso() != null) {
	        curso = cursoRepository.findById(request.idCurso())
	                .orElseThrow(() -> new RecursoNoEncontradoException(Curso.class, request.idCurso()));
	    }
	    
	    Usuario nuevoUsuario = usuarioMapper.toEntity(request, curso);

	    nuevoUsuario.setContrasena(passwordEncoder.encode(request.contrasena()));

	    return usuarioRepository.save(nuevoUsuario);
	}
	
	@Transactional
	public void cambiarEstadoUsuario(Integer idUsuario) {
		Usuario usuario = usuarioRepository.findById(idUsuario)
					.orElseThrow(() -> new RecursoNoEncontradoException(Usuario.class, idUsuario));
		usuario.setActivo(!usuario.isActivo());
		usuarioRepository.save(usuario);	
	}
	
	
	
}
