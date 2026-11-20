package org.zabalburu.lauki.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
import org.zabalburu.lauki.mapper.LogTareaMapper;
import org.zabalburu.lauki.mapper.TareaMapper;
import org.zabalburu.lauki.repository.FaseRepository;
import org.zabalburu.lauki.repository.GrupoRepository;
import org.zabalburu.lauki.repository.MiembroGrupoRepository;
import org.zabalburu.lauki.repository.TareaRepository;
import org.zabalburu.lauki.repository.UsuarioRepository;

@ExtendWith(MockitoExtension.class)
class KanbanTareaServiceTest {

    private static final String CORREO_ACTOR = "estudiante@example.com";
    private static final Integer ID_ACTOR = 1;
    private static final Integer ID_RESPONSABLE = 2;
    private static final Integer ID_GRUPO = 3;
    private static final Integer ID_FASE = 4;
    private static final Integer ID_TAREA = 5;

    @Mock private TareaRepository tareaRepository;
    @Mock private FaseRepository faseRepository;
    @Mock private GrupoRepository grupoRepository;
    @Mock private MiembroGrupoRepository miembroGrupoRepository;
    @Mock private UsuarioRepository usuarioRepository;

    private KanbanTareaService servicio;
    private Usuario actor;
    private Usuario responsable;
    private Reto reto;
    private Grupo grupo;
    private Fase fase;
    private Tarea tarea;

    @BeforeEach
    void prepararTablero() {
        servicio = new KanbanTareaService(tareaRepository, faseRepository, grupoRepository,
                miembroGrupoRepository, usuarioRepository, new TareaMapper(new LogTareaMapper()));
        actor = new Usuario();
        actor.setId(ID_ACTOR);
        actor.setNombre("Estudiante del equipo");
        actor.setRolSistema(RolSistema.ESTUDIANTE);
        actor.setActivo(true);
        when(usuarioRepository.findByCorreoIgnoreCase(CORREO_ACTOR)).thenReturn(Optional.of(actor));

        responsable = new Usuario();
        responsable.setId(ID_RESPONSABLE);
        responsable.setNombre("Marta Ruiz");
        responsable.setActivo(true);

        reto = new Reto();
        reto.setId(7);
        reto.setEstadoReto(EstadoReto.ACTIVO);
        grupo = new Grupo();
        grupo.setId(ID_GRUPO);
        grupo.setReto(reto);
        fase = new Fase();
        fase.setId(ID_FASE);
        fase.setGrupo(grupo);
        tarea = new Tarea();
        tarea.setId(ID_TAREA);
        tarea.setFase(fase);
        tarea.setTitulo("Preparar el informe");
    }

    @Test
    void creaTareaPendienteConEventoDeCreacion() {
        permitirAccesoAlGrupo();
        when(faseRepository.findById(ID_FASE)).thenReturn(Optional.of(fase));
        when(tareaRepository.saveAndFlush(any(Tarea.class))).thenAnswer(invocacion -> {
            Tarea creada = invocacion.getArgument(0);
            creada.setId(ID_TAREA);
            LogTarea evento = creada.getLogs().getFirst();
            assertSame(creada, evento.getTarea());
            assertSame(actor, evento.getUsuario());
            return creada;
        });

        TareaResponse respuesta = servicio.crearTarea(CORREO_ACTOR,
                new CrearTareaRequest("Preparar el informe", "Resumen del reto", ID_FASE, null, null));

        assertEquals(ID_TAREA, respuesta.id());
        assertEquals(ID_FASE, respuesta.faseId());
        assertEquals(EstadoTarea.PENDIENTE, respuesta.estado());
        assertNull(respuesta.responsableId());
        assertNotNull(respuesta.fechaCreacion());
        assertEquals(1, respuesta.logs().size());
        assertEquals(TipoEventoLog.CREACION, respuesta.logs().getFirst().tipoEvento());
    }

    @Test
    void eliminaFisicamenteUnaTareaCompletada() {
        cargarTareaDelGrupo();
        tarea.setEstadoTarea(EstadoTarea.COMPLETADA);

        servicio.eliminarTarea(CORREO_ACTOR, ID_TAREA);

        verify(tareaRepository).delete(tarea);
    }

    @Test
    void asignaYDesasignaUnResponsableDelGrupo() {
        cargarTareaDelGrupo();
        when(usuarioRepository.findById(ID_RESPONSABLE)).thenReturn(Optional.of(responsable));
        when(miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(ID_RESPONSABLE, ID_GRUPO)).thenReturn(true);

        TareaResponse asignada = servicio.asignarResponsable(CORREO_ACTOR, ID_TAREA, ID_RESPONSABLE);
        assertSame(responsable, tarea.getUsuario());
        assertEquals(ID_RESPONSABLE, asignada.responsableId());

        servicio.asignarResponsable(CORREO_ACTOR, ID_TAREA, null);
        assertNull(tarea.getUsuario());
    }

    @Test
    void rechazaUnResponsableDeOtroGrupo() {
        cargarTareaDelGrupo();
        when(usuarioRepository.findById(ID_RESPONSABLE)).thenReturn(Optional.of(responsable));

        assertThrows(ResponsableAjenoAlGrupoException.class,
                () -> servicio.asignarResponsable(CORREO_ACTOR, ID_TAREA, ID_RESPONSABLE));
        assertNull(tarea.getUsuario());
    }

    @Test
    void rechazaUnResponsableInactivo() {
        cargarTareaDelGrupo();
        responsable.setActivo(false);
        when(usuarioRepository.findById(ID_RESPONSABLE)).thenReturn(Optional.of(responsable));
        when(miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(ID_RESPONSABLE, ID_GRUPO)).thenReturn(true);

        assertThrows(RecursoInactivoException.class,
                () -> servicio.asignarResponsable(CORREO_ACTOR, ID_TAREA, ID_RESPONSABLE));
        assertNull(tarea.getUsuario());
    }

    @Test
    void conservaElResponsableDeUnaTareaCompletada() {
        cargarTareaDelGrupo();
        tarea.setEstadoTarea(EstadoTarea.COMPLETADA);
        tarea.setUsuario(responsable);

        assertThrows(TareaSinResponsableException.class,
                () -> servicio.asignarResponsable(CORREO_ACTOR, ID_TAREA, null));
        assertSame(responsable, tarea.getUsuario());
    }

    @Test
    void rechazaMoverUnaTareaSinResponsable() {
        cargarTareaDelGrupo();

        assertThrows(TareaSinResponsableException.class,
                () -> cambiarEstado(EstadoTarea.EN_PROGRESO));

        assertEquals(EstadoTarea.PENDIENTE, tarea.getEstadoTarea());
        assertNull(tarea.getFechaInicializacion());
        assertTrue(tarea.getLogs().isEmpty());
        verify(tareaRepository, never()).flush();
    }

    @ParameterizedTest
    @EnumSource(value = EstadoTarea.class, names = {"EN_PROGRESO", "EN_SUPERVISION", "COMPLETADA"})
    void mueveDesdePendienteYRegistraLaTransicion(EstadoTarea destino) {
        cargarTareaDelGrupo();
        tarea.setUsuario(responsable);

        TareaResponse respuesta = cambiarEstado(destino);

        assertEquals(destino, respuesta.estado());
        assertNotNull(respuesta.fechaInicializacion());
        LogTarea evento = tarea.getLogs().getFirst();
        assertEquals(TipoEventoLog.TRANSICION, evento.getTipoEventoLog());
        assertEquals(EstadoTarea.PENDIENTE, evento.getEstadoAnterior());
        assertEquals(destino, evento.getEstadoNuevo());
        assertSame(actor, evento.getUsuario());
        assertSame(tarea, evento.getTarea());
        verify(tareaRepository).flush();
    }

    @Test
    void limpiaLaFechaDeCierreAlReabrirYConservaElPrimerInicio() {
        cargarTareaDelGrupo();
        tarea.setUsuario(responsable);

        TareaResponse completada = cambiarEstado(EstadoTarea.COMPLETADA);
        assertNotNull(completada.fechaFinalizacionReal());
        TareaResponse reabierta = cambiarEstado(EstadoTarea.PENDIENTE);
        assertNull(reabierta.fechaFinalizacionReal());
        TareaResponse enProgreso = cambiarEstado(EstadoTarea.EN_PROGRESO);

        assertEquals(completada.fechaInicializacion(), enProgreso.fechaInicializacion());
        assertEquals(3, tarea.getLogs().size());
    }

    @Test
    void bloqueaConMotivoYPermiteDesbloquearConUnaTransicion() {
        cargarTareaDelGrupo();
        tarea.setUsuario(responsable);

        TareaResponse bloqueada = servicio.bloquear(CORREO_ACTOR,
                new BloqueoTareaRequest(ID_TAREA, "  Pendiente de validación  "));

        assertEquals(EstadoTarea.BLOQUEADA, bloqueada.estado());
        assertNotNull(bloqueada.fechaInicializacion());
        assertEquals("Pendiente de validación", bloqueada.ultimoMotivoBloqueo());
        LogTarea evento = tarea.getLogs().getFirst();
        assertEquals(TipoEventoLog.BLOQUEO, evento.getTipoEventoLog());
        assertEquals(EstadoTarea.PENDIENTE, evento.getEstadoAnterior());
        assertEquals(EstadoTarea.BLOQUEADA, evento.getEstadoNuevo());
        assertSame(actor, evento.getUsuario());

        assertEquals(EstadoTarea.EN_PROGRESO, cambiarEstado(EstadoTarea.EN_PROGRESO).estado());
        assertEquals(2, tarea.getLogs().size());
    }

    @Test
    void rechazaElBloqueoSinMotivoYElMovimientoDirectoABloqueada() {
        cargarTareaDelGrupo();
        tarea.setUsuario(responsable);

        assertThrows(TransicionEstadoInvalidaException.class,
                () -> servicio.bloquear(CORREO_ACTOR, new BloqueoTareaRequest(ID_TAREA, "  ")));
        assertThrows(TransicionEstadoInvalidaException.class, () -> cambiarEstado(EstadoTarea.BLOQUEADA));
        assertEquals(EstadoTarea.PENDIENTE, tarea.getEstadoTarea());
        assertTrue(tarea.getLogs().isEmpty());
        verify(tareaRepository, never()).flush();
    }

    @Test
    void alBloquearSinResponsablePrevaleceEseErrorSobreElMotivo() {
        cargarTareaDelGrupo();

        assertThrows(TareaSinResponsableException.class,
                () -> servicio.bloquear(CORREO_ACTOR, new BloqueoTareaRequest(ID_TAREA, "")));
        assertTrue(tarea.getLogs().isEmpty());
    }

    @Test
    void agregaComentarioAlHistorialSinCambiarElEstado() {
        cargarTareaDelGrupo();

        TareaResponse respuesta = servicio.comentar(CORREO_ACTOR, ID_TAREA,
                new ComentarioRequest("  Falta adjuntar el informe  "));

        assertEquals(EstadoTarea.PENDIENTE, respuesta.estado());
        assertEquals(1, respuesta.totalComentarios());
        LogTarea evento = tarea.getLogs().getFirst();
        assertEquals(TipoEventoLog.COMENTARIO, evento.getTipoEventoLog());
        assertEquals("Falta adjuntar el informe", evento.getComentario());
        assertSame(actor, evento.getUsuario());
        verify(tareaRepository).flush();
    }

    @Test
    void rechazaUnComentarioEnBlanco() {
        cargarTareaDelGrupo();

        assertThrows(ReglaNegocioException.class,
                () -> servicio.comentar(CORREO_ACTOR, ID_TAREA, new ComentarioRequest("  ")));
        assertTrue(tarea.getLogs().isEmpty());
        verify(tareaRepository, never()).flush();
    }

    @Test
    void rechazaEditarUnaTareaDeOtroGrupo() {
        when(tareaRepository.findById(ID_TAREA)).thenReturn(Optional.of(tarea));

        assertThrows(UsuarioNoPerteneceAlGrupoException.class,
                () -> servicio.actualizarDescripcion(CORREO_ACTOR, ID_TAREA, "Nueva descripción"));
        assertNull(tarea.getDescripcion());
    }

    @Test
    void rechazaElTrasladoAUnaFaseAjenaAlGrupo() {
        cargarTareaDelGrupo();
        Integer idFaseAjena = 8;
        when(faseRepository.findByIdAndGrupo_Id(idFaseAjena, ID_GRUPO)).thenReturn(Optional.empty());

        assertThrows(RecursoNoEncontradoException.class,
                () -> servicio.editarTarea(CORREO_ACTOR, ID_TAREA, "Nuevo título", idFaseAjena, null));
        assertSame(fase, tarea.getFase());
        assertEquals("Preparar el informe", tarea.getTitulo());
    }

    @Test
    void impideEliminarTareasDeUnRetoFinalizado() {
        cargarTareaDelGrupo();
        reto.setEstadoReto(EstadoReto.FINALIZADO);

        assertThrows(RetoNoEditableException.class, () -> servicio.eliminarTarea(CORREO_ACTOR, ID_TAREA));
        verify(tareaRepository, never()).delete(any(Tarea.class));
    }

    @Test
    void permiteQueElAdministradorConsultePeroNoModifiqueTareas() {
        actor.setRolSistema(RolSistema.ADMINISTRADOR);
        when(grupoRepository.findById(ID_GRUPO)).thenReturn(Optional.of(grupo));
        when(tareaRepository.findAllByFase_Grupo_Id(ID_GRUPO)).thenReturn(List.of(tarea));
        when(tareaRepository.findById(ID_TAREA)).thenReturn(Optional.of(tarea));

        assertEquals(ID_TAREA, servicio.listarTareas(CORREO_ACTOR, ID_GRUPO).getFirst().id());
        assertThrows(UsuarioNoPerteneceAlGrupoException.class,
                () -> servicio.eliminarTarea(CORREO_ACTOR, ID_TAREA));
        verify(tareaRepository, never()).delete(any(Tarea.class));
    }

    private void permitirAccesoAlGrupo() {
        when(miembroGrupoRepository.existsByUsuario_IdAndGrupo_Id(ID_ACTOR, ID_GRUPO)).thenReturn(true);
    }

    private void cargarTareaDelGrupo() {
        permitirAccesoAlGrupo();
        when(tareaRepository.findById(ID_TAREA)).thenReturn(Optional.of(tarea));
    }

    private TareaResponse cambiarEstado(EstadoTarea destino) {
        return servicio.cambiarEstado(CORREO_ACTOR, ID_TAREA, new CambiarEstadoTareaRequest(destino));
    }
}
