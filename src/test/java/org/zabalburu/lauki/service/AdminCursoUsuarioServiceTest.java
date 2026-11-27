package org.zabalburu.lauki.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.zabalburu.lauki.domain.Curso;
import org.zabalburu.lauki.exception.FormatoPeriodoInvalidoException;
import org.zabalburu.lauki.exception.RecursoDuplicadoException;
import org.zabalburu.lauki.mapper.UsuarioMapper;
import org.zabalburu.lauki.repository.CursoRepository;
import org.zabalburu.lauki.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class AdminCursoUsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UsuarioMapper usuarioMapper;

    private AdminCursoUsuarioService service;

    @BeforeEach
    void setUp() {
        service = new AdminCursoUsuarioService(usuarioRepository, cursoRepository, passwordEncoder, usuarioMapper);
    }

    @Test
    void crearCurso_conDatosValidos_guardaCorrectamente() {
        when(cursoRepository.existsByNombreAndPeriodoAcademico("1º DAW", "2026/2027")).thenReturn(false);
        when(cursoRepository.save(any(Curso.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Curso creado = service.crearCurso("1º DAW", "2026/2027");

        assertNotNull(creado);
        assertEquals("1º DAW", creado.getNombre());
        assertEquals("2026/2027", creado.getPeriodoAcademico());
        verify(cursoRepository).save(any(Curso.class));
    }

    @Test
    void crearCurso_conFormatoInvalido_lanzaFormatoPeriodoInvalidoException() {
        assertThrows(FormatoPeriodoInvalidoException.class, () -> service.crearCurso("1º DAW", "2026-2027"));
        assertThrows(FormatoPeriodoInvalidoException.class, () -> service.crearCurso("1º DAW", "26/27"));
        assertThrows(FormatoPeriodoInvalidoException.class, () -> service.crearCurso("1º DAW", null));
    }

    @Test
    void crearCurso_conMismoNombreYPeriodo_lanzaRecursoDuplicadoException() {
        when(cursoRepository.existsByNombreAndPeriodoAcademico("1º DAW", "2026/2027")).thenReturn(true);

        assertThrows(RecursoDuplicadoException.class, () -> service.crearCurso("1º DAW", "2026/2027"));
    }
}
