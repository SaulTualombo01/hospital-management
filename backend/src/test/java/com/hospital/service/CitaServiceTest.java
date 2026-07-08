package com.hospital.service;

import com.hospital.dto.CitaDTO;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Cita;
import com.hospital.model.Doctor;
import com.hospital.repository.CitaRepository;
import com.hospital.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CitaServiceTest {

    private CitaRepository citaRepository;
    private DoctorRepository doctorRepository;
    private CitaService citaService;

    @BeforeEach
    void setUp() {
        citaRepository = mock(CitaRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        citaService = new CitaService(citaRepository, doctorRepository);
    }

    @Test
    void listarTodas_debeRetornarLista() {
        Cita c1 = new Cita(); Cita c2 = new Cita();
        when(citaRepository.findAll()).thenReturn(Arrays.asList(c1, c2));

        List<Cita> resultado = citaService.listarTodas();

        assertEquals(2, resultado.size());
        verify(citaRepository).findAll();
    }

    @Test
    void buscarPorId_existente_debeRetornarCita() {
        Cita c = new Cita(); c.setId(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(c));

        Cita resultado = citaService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorId_inexistente_debeLanzarExcepcion() {
        when(citaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> citaService.buscarPorId(99L));
    }

    @Test
    void crear_debeGuardarCitaConDoctor() {
        Doctor doctor = new Doctor(); doctor.setId(2L);
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        CitaDTO dto = new CitaDTO();
        dto.setPacienteId(1L);
        dto.setDoctorId(2L);
        dto.setFechaHora(LocalDateTime.now().plusDays(1));
        dto.setMotivo("Consulta general");

        Cita resultado = citaService.crear(dto);

        assertEquals("Consulta general", resultado.getMotivo());
        assertEquals(doctor, resultado.getDoctor());
        assertEquals("PROGRAMADA", resultado.getEstado());
        verify(citaRepository).save(any(Cita.class));
    }

    @Test
    void crear_conDoctorInexistente_debeLanzarExcepcion() {
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        CitaDTO dto = new CitaDTO();
        dto.setPacienteId(1L);
        dto.setDoctorId(2L);
        dto.setFechaHora(LocalDateTime.now().plusDays(1));

        assertThrows(ResourceNotFoundException.class,
                () -> citaService.crear(dto));
    }

    @Test
    void actualizar_debeModificarCitaExistente() {
        Cita existente = new Cita(); existente.setId(1L);
        when(citaRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(citaRepository.save(any(Cita.class))).thenAnswer(inv -> inv.getArgument(0));

        CitaDTO dto = new CitaDTO();
        dto.setFechaHora(LocalDateTime.now().plusDays(2));
        dto.setMotivo("Control");
        dto.setEstado("COMPLETADA");

        Cita resultado = citaService.actualizar(1L, dto);

        assertEquals("Control", resultado.getMotivo());
        assertEquals("COMPLETADA", resultado.getEstado());
        verify(citaRepository).save(existente);
    }

    @Test
    void eliminar_debeInvocarDeleteById() {
        citaService.eliminar(1L);
        verify(citaRepository).deleteById(1L);
    }

    @Test
    void listarPorPaciente_debeInvocarRepositorio() {
        citaService.listarPorPaciente(1L);
        verify(citaRepository).findByPacienteId(1L);
    }

    @Test
    void listarPorDoctor_debeInvocarRepositorio() {
        citaService.listarPorDoctor(2L);
        verify(citaRepository).findByDoctorId(2L);
    }

    @Test
    void listarPorEstado_debeInvocarRepositorio() {
        citaService.listarPorEstado("PROGRAMADA");
        verify(citaRepository).findCitasByEstadoOrdered("PROGRAMADA");
    }

    @Test
    void listarPorRangoFechas_debeInvocarRepositorio() {
        LocalDateTime inicio = LocalDateTime.now();
        LocalDateTime fin = inicio.plusDays(5);

        citaService.listarPorRangoFechas(inicio, fin);

        verify(citaRepository).findByFechaHoraBetween(inicio, fin);
    }
}
