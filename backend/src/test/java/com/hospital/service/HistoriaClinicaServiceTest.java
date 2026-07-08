package com.hospital.service;

import com.hospital.dto.HistoriaClinicaDTO;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Doctor;
import com.hospital.model.HistoriaClinica;
import com.hospital.model.Paciente;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.HistoriaClinicaRepository;
import com.hospital.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HistoriaClinicaServiceTest {

    private HistoriaClinicaRepository historiaRepository;
    private PacienteRepository pacienteRepository;
    private DoctorRepository doctorRepository;
    private HistoriaClinicaService historiaService;

    @BeforeEach
    void setUp() {
        historiaRepository = mock(HistoriaClinicaRepository.class);
        pacienteRepository = mock(PacienteRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        historiaService = new HistoriaClinicaService(historiaRepository, pacienteRepository, doctorRepository);
    }

    @Test
    void listarTodas_debeRetornarLista() {
        HistoriaClinica h1 = new HistoriaClinica();
        HistoriaClinica h2 = new HistoriaClinica();
        when(historiaRepository.findAllByOrderByFechaCreacionDesc()).thenReturn(Arrays.asList(h1, h2));

        List<HistoriaClinica> resultado = historiaService.listarTodas();

        assertEquals(2, resultado.size());
        verify(historiaRepository).findAllByOrderByFechaCreacionDesc();
    }

    @Test
    void buscarPorId_existente_debeRetornarHistoria() {
        HistoriaClinica h = new HistoriaClinica(); h.setId(1L);
        when(historiaRepository.findById(1L)).thenReturn(Optional.of(h));

        HistoriaClinica resultado = historiaService.buscarPorId(1L);

        assertEquals(1L, resultado.getId());
    }

    @Test
    void buscarPorId_inexistente_debeLanzarExcepcion() {
        when(historiaRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> historiaService.buscarPorId(99L));
    }

    @Test
    void crear_debeGuardarHistoriaConPacienteYDoctor() {
        Paciente paciente = new Paciente(); paciente.setId(1L);
        Doctor doctor = new Doctor(); doctor.setId(2L);

        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctor));
        when(historiaRepository.save(any(HistoriaClinica.class))).thenAnswer(inv -> inv.getArgument(0));

        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setPacienteId(1L);
        dto.setDoctorId(2L);
        dto.setDiagnostico("Dolor de cabeza");
        dto.setTratamiento("Paracetamol");
        dto.setObservaciones("Revisar en 1 semana");

        HistoriaClinica resultado = historiaService.crear(dto);

        assertEquals("Dolor de cabeza", resultado.getDiagnostico());
        assertEquals(paciente, resultado.getPaciente());
        assertEquals(doctor, resultado.getDoctor());
        verify(historiaRepository).save(any(HistoriaClinica.class));
    }

    @Test
    void crear_conPacienteInexistente_debeLanzarExcepcion() {
        when(pacienteRepository.findById(1L)).thenReturn(Optional.empty());

        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setPacienteId(1L);
        dto.setDiagnostico("Dolor de cabeza");

        assertThrows(ResourceNotFoundException.class,
                () -> historiaService.crear(dto));
    }

    @Test
    void crear_conDoctorInexistente_debeLanzarExcepcion() {
        Paciente paciente = new Paciente(); paciente.setId(1L);
        when(pacienteRepository.findById(1L)).thenReturn(Optional.of(paciente));
        when(doctorRepository.findById(2L)).thenReturn(Optional.empty());

        HistoriaClinicaDTO dto = new HistoriaClinicaDTO();
        dto.setPacienteId(1L);
        dto.setDoctorId(2L);
        dto.setDiagnostico("Dolor de cabeza");

        assertThrows(ResourceNotFoundException.class,
                () -> historiaService.crear(dto));
    }

    @Test
    void listarPorPaciente_debeInvocarRepositorio() {
        historiaService.listarPorPaciente(1L);
        verify(historiaRepository).findByPacienteId(1L);
    }

    @Test
    void listarPorDoctor_debeInvocarRepositorio() {
        historiaService.listarPorDoctor(2L);
        verify(historiaRepository).findByDoctorId(2L);
    }
}
