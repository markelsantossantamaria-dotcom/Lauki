package org.zabalburu.lauki.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.zabalburu.lauki.domain.Curso;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.domain.enums.RolSistema;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {
	
	//Para filtrar usuarios por su rol
	List<Usuario> findAllByRolSistema(RolSistema rolSistema);

	//Para filtrar usuarios por cursos
	List<Usuario> findAllByCurso_Id(Integer idCurso);
	
	//Para filtrar a un usuario por nombre
	List<Usuario> findByNombreContainingIgnoreCase(String nombre);

	//Para filtrar un usuario por correo
	Optional<Usuario> findByCorreoIgnoreCase(String correo);
	
	//Para filtrar usuarios estan activos / Para dar de baja en masa usuarios al terminar el ciclo formativo
	List<Usuario> findAllByActivo(boolean activo);
	
	List<Usuario> findAllByCurso_IdAndActivo(Integer idCurso, boolean activo);
	
	boolean existsByCorreo(String correo);
	
	
	
}
