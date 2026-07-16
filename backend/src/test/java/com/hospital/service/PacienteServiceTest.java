package com.hospital.service;

import com.hospital.dto.PacienteDTO;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Paciente;
import com.hospital.repository.PacienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.Month;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PacienteServiceTest {

    @Mock
    private PacienteRepository pacienteRepository;

    @InjectMocks
    private PacienteService pacienteService;

    @Test
    void listarTodosDebeRetornarLaListaDelRepositorio() {
        List<Paciente> pacientes = List.of(
                crearPaciente("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10)),
                crearPaciente("Carlos", "Perez", LocalDate.of(1988, Month.MAY, 20))
        );

        when(pacienteRepository.findAll()).thenReturn(pacientes);

        List<Paciente> resultado = pacienteService.listarTodos();

        assertEquals(2, resultado.size());
        assertEquals("Ana", resultado.get(0).getNombre());
        assertEquals("Carlos", resultado.get(1).getNombre());
        verify(pacienteRepository).findAll();
    }

    @Test
    void buscarPorIdDebeRetornarPacienteCuandoExiste() {
        Paciente paciente = crearPaciente("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10));
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        Paciente resultado = pacienteService.buscarPorId(1L);

        assertEquals("Ana", resultado.getNombre());
        assertEquals("Lopez", resultado.getApellido());
        verify(pacienteRepository).findById(1L);
    }

    @Test
    void buscarPorIdDebeLanzarExceptionCuandoNoExiste() {
        when(pacienteRepository.findById(99L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> pacienteService.buscarPorId(99L)
        );

        assertTrue(exception.getMessage().contains("99"));
        verify(pacienteRepository).findById(99L);
    }

    @Test
    void crearDebeConvertirDtoYGuardarPaciente() {
        PacienteDTO dto = crearDto("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10));
        Paciente pacienteGuardado = crearPaciente("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10));
        when(pacienteRepository.save(any(Paciente.class))).thenReturn(pacienteGuardado);

        Paciente resultado = pacienteService.crear(dto);

        ArgumentCaptor<Paciente> captor = ArgumentCaptor.forClass(Paciente.class);
        verify(pacienteRepository).save(captor.capture());

        Paciente enviadoAlRepositorio = captor.getValue();
        assertEquals("Ana", enviadoAlRepositorio.getNombre());
        assertEquals("Lopez", enviadoAlRepositorio.getApellido());
        assertEquals(LocalDate.of(1995, Month.JANUARY, 10), enviadoAlRepositorio.getFechaNacimiento());
        assertEquals("ana@test.com", enviadoAlRepositorio.getEmail());
        assertEquals("0991234567", enviadoAlRepositorio.getTelefono());
        assertEquals("Quito", enviadoAlRepositorio.getDireccion());
        assertTrue(enviadoAlRepositorio.getActivo());

        assertEquals("Ana", resultado.getNombre());
    }

    @Test
    void actualizarDebeModificarCamposYGuardarPaciente() {
        Paciente existente = crearPaciente("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10));
        existente.setActivo(true);

        PacienteDTO dto = crearDto("Maria", "Gomez", LocalDate.of(1990, Month.FEBRUARY, 15));
        dto.setActivo(false);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(pacienteRepository.save(any(Paciente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Paciente resultado = pacienteService.actualizar(1L, dto);

        assertEquals("Maria", resultado.getNombre());
        assertEquals("Gomez", resultado.getApellido());
        assertEquals(LocalDate.of(1990, Month.FEBRUARY, 15), resultado.getFechaNacimiento());
        assertEquals("ana@test.com", resultado.getEmail());
        assertEquals("0991234567", resultado.getTelefono());
        assertEquals("Quito", resultado.getDireccion());
        assertFalse(resultado.getActivo());
        verify(pacienteRepository).findById(1L);
        verify(pacienteRepository).save(existente);
    }

    @Test
    void buscarPorNombreConNullDebePropagarElErrorDelRepositorio() {
        when(pacienteRepository.buscarPorNombre(null)).thenThrow(new NullPointerException("nombre"));

        assertThrows(NullPointerException.class, () -> pacienteService.buscarPorNombre(null));

        verify(pacienteRepository).buscarPorNombre(null);
    }

    @Test
    void eliminarDebeBuscarYEliminarPaciente() {
        Paciente paciente = crearPaciente("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10));
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));

        pacienteService.eliminar(1L);

        verify(pacienteRepository).findById(1L);
        verify(pacienteRepository).delete(paciente);
    }

    @Test
    void buscarPorNombreDebeDelegarEnRepositorio() {
        List<Paciente> pacientes = List.of(crearPaciente("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10)));
        when(pacienteRepository.buscarPorNombre("Ana")).thenReturn(pacientes);

        List<Paciente> resultado = pacienteService.buscarPorNombre("Ana");

        assertEquals(1, resultado.size());
        assertEquals("Ana", resultado.get(0).getNombre());
        verify(pacienteRepository).buscarPorNombre("Ana");
    }

    @Test
    void buscarPorEmailDebeDelegarEnRepositorio() {
        Paciente paciente = crearPaciente("Ana", "Lopez", LocalDate.of(1995, Month.JANUARY, 10));
        when(pacienteRepository.findByEmail("ana@test.com")).thenReturn(paciente);

        Paciente resultado = pacienteService.buscarPorEmail("ana@test.com");

        assertEquals("Ana", resultado.getNombre());
        assertEquals("Lopez", resultado.getApellido());
        verify(pacienteRepository).findByEmail("ana@test.com");
    }

    @Test
    void calcularEdadPromedioDebeRetornarPromedioCorrecto() {
        Paciente p1 = crearPaciente("Ana", "Lopez", LocalDate.now().minusYears(20));
        Paciente p2 = crearPaciente("Carlos", "Perez", LocalDate.now().minusYears(30));
        when(pacienteRepository.findAll()).thenReturn(List.of(p1, p2));

        double resultado = pacienteService.calcularEdadPromedio();

        assertEquals(25.0, resultado);
        verify(pacienteRepository).findAll();
    }

    @Test
    void calcularEdadPromedioSinPacientesDebeExponerElProblemaDeDivisionPorCero() {
        when(pacienteRepository.findAll()).thenReturn(Collections.emptyList());

        double resultado = pacienteService.calcularEdadPromedio();

        assertTrue(Double.isNaN(resultado));
        verify(pacienteRepository).findAll();
    }

    private PacienteDTO crearDto(String nombre, String apellido, LocalDate fechaNacimiento) {
        PacienteDTO dto = new PacienteDTO();
        dto.setNombre(nombre);
        dto.setApellido(apellido);
        dto.setFechaNacimiento(fechaNacimiento);
        dto.setEmail("ana@test.com");
        dto.setTelefono("0991234567");
        dto.setDireccion("Quito");
        dto.setActivo(true);
        return dto;
    }

    private Paciente crearPaciente(String nombre, String apellido, LocalDate fechaNacimiento) {
        Paciente paciente = new Paciente();
        paciente.setNombre(nombre);
        paciente.setApellido(apellido);
        paciente.setFechaNacimiento(fechaNacimiento);
        paciente.setEmail("ana@test.com");
        paciente.setTelefono("0991234567");
        paciente.setDireccion("Quito");
        paciente.setActivo(true);
        return paciente;
    }
}