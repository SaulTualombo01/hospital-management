package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Doctor;
import com.hospital.repository.DoctorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DoctorServiceTest {

    private DoctorRepository doctorRepository;
    private DoctorService doctorService;

    @BeforeEach
    void setUp() {
        doctorRepository = mock(DoctorRepository.class);
        doctorService = new DoctorService(doctorRepository);
    }

    @Test
    void listarTodos_debeRetornarListaDeDoctores() {
        Doctor d1 = new Doctor();
        d1.setNombre("Ana");
        Doctor d2 = new Doctor();
        d2.setNombre("Carlos");

        when(doctorRepository.findAll()).thenReturn(Arrays.asList(d1, d2));

        List<Doctor> resultado = doctorService.listarTodos();

        assertEquals(2, resultado.size());
        verify(doctorRepository, times(1)).findAll();
    }

    @Test
    void buscarPorId_existente_debeRetornarDoctor() {
        Doctor d = new Doctor();
        d.setId(1L);
        d.setNombre("Ana");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(d));

        Doctor resultado = doctorService.buscarPorId(1L);

        assertEquals("Ana", resultado.getNombre());
    }

    @Test
    void buscarPorId_inexistente_debeLanzarExcepcion() {
        when(doctorRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> doctorService.buscarPorId(99L));
    }

    @Test
    void crear_debeGuardarDoctor() {
        DoctorDTO dto = new DoctorDTO();
        dto.setNombre("Ana");
        dto.setApellido("Perez");
        dto.setEspecialidad("Cardiología");
        dto.setEmail("ana@hospital.com");
        dto.setTelefono("12345");
        dto.setConsultorio("C1");

        Doctor doctorGuardado = new Doctor();
        doctorGuardado.setId(1L);

        when(doctorRepository.save(any(Doctor.class))).thenReturn(doctorGuardado);

        Doctor resultado = doctorService.crear(dto);

        assertNotNull(resultado.getId());
        verify(doctorRepository).save(any(Doctor.class));
    }


    @Test
    void actualizar_debeModificarDoctorExistente() {
        // Simulamos un doctor existente en la BD
        Doctor existente = new Doctor();
        existente.setId(1L);
        existente.setNombre("Ana");

        when(doctorRepository.findById(1L)).thenReturn(Optional.of(existente));
        when(doctorRepository.save(any(Doctor.class))).thenAnswer(inv -> inv.getArgument(0));

        // Creamos el DTO con los nuevos datos usando setters
        DoctorDTO dto = new DoctorDTO();
        dto.setNombre("AnaMaria");
        dto.setApellido("Perez");
        dto.setEspecialidad("Cardiología");
        dto.setEmail("ana@hospital.com");
        dto.setTelefono("12345");
        dto.setConsultorio("C1");

        // Ejecutamos la actualización
        Doctor resultado = doctorService.actualizar(1L, dto);

        // Verificamos que se haya modificado correctamente
        assertEquals("AnaMaria", resultado.getNombre());
        assertEquals("Perez", resultado.getApellido());
        assertEquals("Cardiología", resultado.getEspecialidad());
        assertEquals("ana@hospital.com", resultado.getEmail());
        assertEquals("12345", resultado.getTelefono());
        assertEquals("C1", resultado.getConsultorio());

        // Confirmamos que se llamó al repositorio para guardar
        verify(doctorRepository).save(existente);
    }


    @Test
    void eliminar_debeInvocarDeleteById() {
        doctorService.eliminar(1L);
        verify(doctorRepository).deleteById(1L);
    }

    @Test
    void buscarPorNombreCompleto_debeInvocarRepositorio() {
        doctorService.buscarPorNombreCompleto("Ana", "Perez");
        verify(doctorRepository).findByNombreAndApellido("Ana", "Perez");
    }
}
