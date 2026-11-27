package org.zabalburu.lauki.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.zabalburu.lauki.domain.enums.RolSistema;
import org.zabalburu.lauki.domain.Usuario;
import org.zabalburu.lauki.repository.UsuarioRepository;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        String correoAdmin = "admin@zabalburu.org";

        if (!usuarioRepository.existsByCorreo(correoAdmin)) {
            Usuario admin = new Usuario();
            admin.setNombre("Administrador");
            admin.setCorreo(correoAdmin);
            admin.setContrasena(passwordEncoder.encode("admin123")); 
            admin.setRolSistema(RolSistema.ADMINISTRADOR);
            admin.setActivo(true);

            usuarioRepository.save(admin);
        }
        
        String correoEstudiante = "estudiante@zabalburu.org";

        if (!usuarioRepository.existsByCorreo(correoEstudiante)) {
            Usuario estudiante = new Usuario();
            estudiante.setNombre("Estudiante");
            estudiante.setCorreo(correoEstudiante);
            estudiante.setContrasena(passwordEncoder.encode("estudiante123"));
            estudiante.setRolSistema(RolSistema.ESTUDIANTE);
            estudiante.setActivo(true);

            usuarioRepository.save(estudiante);
        }
    }
}