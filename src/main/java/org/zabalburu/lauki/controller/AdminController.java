package org.zabalburu.lauki.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.zabalburu.lauki.domain.enums.RolSistema;
import org.zabalburu.lauki.dto.request.CrearUsuarioRequest;
import org.zabalburu.lauki.service.AdminCursoUsuarioService;
import org.zabalburu.lauki.service.AdminGrupoRetoService;

@Controller
public class AdminController {

	private final AdminCursoUsuarioService adminCursoUsuarioService;
	private final AdminGrupoRetoService adminGrupoRetoService;
	
	@Autowired
	public AdminController(
			AdminCursoUsuarioService adminCursoUsuarioService,
			AdminGrupoRetoService adminGrupoRetoService) {
		this.adminCursoUsuarioService = adminCursoUsuarioService;
		this.adminGrupoRetoService = adminGrupoRetoService;
		
	}
	
	
	@GetMapping({"/admin", "/admin/panel"})
	public String mostrarPanel(Model model) {
	    model.addAttribute("seccionActiva", "inicio");
	    model.addAttribute("totalCursos", adminCursoUsuarioService.listarTodosLosCursosActivos().size());
	    model.addAttribute("totalUsuarios", adminCursoUsuarioService.listarTodosLosUsuarios().size());
	    model.addAttribute("totalGrupos", adminGrupoRetoService.listarTodosLosGrupos().size());
	    model.addAttribute("totalRetos", adminGrupoRetoService.listarTodosLosRetos().size());
	    return "admin/panel";
	}
	
	@GetMapping("/admin/cursos")
	public String listarCursos(Model model) {
	    model.addAttribute("seccionActiva", "cursos");
	    model.addAttribute("cursos", adminCursoUsuarioService.listarTodosLosCursos());
	    return "admin/cursos";
	}

	@PostMapping("/admin/cursos")
	public String crearCurso(@RequestParam String nombre) {
	    adminCursoUsuarioService.crearCurso(nombre);
	    return "redirect:/admin/cursos";
	}
	
	
	@GetMapping("/admin/usuarios")
	public String listarUsuarios(Model model) {
	    model.addAttribute("seccionActiva", "usuarios");
	    model.addAttribute("usuarios", adminCursoUsuarioService.listarTodosLosUsuarios());
	    model.addAttribute("cursos", adminCursoUsuarioService.listarTodosLosCursosActivos());
	    return "admin/usuarios";
	}

	@PostMapping("/admin/usuarios")
	public String crearUsuario(
	        @RequestParam String nombre,
	        @RequestParam String email,
	        @RequestParam String password,
	        @RequestParam Integer cursoId) {

	    CrearUsuarioRequest request = new CrearUsuarioRequest(
	            cursoId,
	    		nombre,
	            email,
	            password,
	            RolSistema.ESTUDIANTE
	    );

	    adminCursoUsuarioService.crearUsuario(request);
	    return "redirect:/admin/usuarios";
	}
	
	@GetMapping("/admin/retos")
	public String listarRetos(Model model) {
	    model.addAttribute("seccionActiva", "retos");
	    model.addAttribute("retos", adminGrupoRetoService.listarTodosLosRetos());
	    model.addAttribute("cursos", adminCursoUsuarioService.listarTodosLosCursosActivos());
	    return "admin/retos";
	}

	@PostMapping("/admin/retos")
	public String crearReto(
	        @RequestParam Integer cursoId,
	        @RequestParam String nombre,
	        @RequestParam String descripcion) {
	    adminGrupoRetoService.crearReto(cursoId, nombre, descripcion);
	    return "redirect:/admin/retos";
	}

	@PostMapping("/admin/retos/{id}/finalizar")
	public String finalizarReto(@PathVariable Integer id) {
	    adminGrupoRetoService.finalizarReto(id);
	    return "redirect:/admin/retos";
	}
	
	@GetMapping("/admin/grupos")
	public String listarGrupos(Model model) {
	    model.addAttribute("seccionActiva", "grupos");
	    model.addAttribute("grupos", adminGrupoRetoService.listarTodosLosGrupos());
	    model.addAttribute("retos", adminGrupoRetoService.listarTodosLosRetos());
	    model.addAttribute("alumnosDisponibles", adminCursoUsuarioService.listarTodosLosUsuariosActivos());
	    return "admin/grupos";
	}

	@PostMapping("/admin/grupos")
	public String crearGrupo(
	        @RequestParam Integer retoId,
	        @RequestParam String nombre) {
	    adminGrupoRetoService.crearGrupo(retoId, nombre);
	    return "redirect:/admin/grupos";
	}

	@PostMapping("/admin/grupos/{id}/miembros")
	public String asignarMiembroAGrupo(
	        @PathVariable Integer id,
	        @RequestParam Integer usuarioId) {
	    adminGrupoRetoService.asignarMiembroAGrupo(id, usuarioId);
	    return "redirect:/admin/grupos";
	}
}
