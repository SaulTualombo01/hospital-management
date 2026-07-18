package com.hospital.service;

import com.hospital.dto.CitaDTO;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Cita;
import com.hospital.model.Doctor;
import com.hospital.repository.CitaRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PacienteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true) // FIX: se agrega @Transactional a nivel de clase, solo lectura por defecto
public class CitaService {

    private final CitaRepository citaRepository;
    private final DoctorRepository doctorRepository;
    private final PacienteRepository pacienteRepository; // FIX: se necesita para validar existencia del paciente

    public CitaService(CitaRepository citaRepository, DoctorRepository doctorRepository,
                       PacienteRepository pacienteRepository) {
        this.citaRepository = citaRepository;
        this.doctorRepository = doctorRepository;
        this.pacienteRepository = pacienteRepository;
    }

    public List<Cita> listarTodas() {
        return citaRepository.findAll();
    }

    public Cita buscarPorId(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada con ID: " + id));
    }

    @Transactional // FIX: sobreescribe el readOnly de la clase, ahora con rollback automatico si falla
    public Cita crear(CitaDTO dto) {
        Doctor doctor = doctorRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado"));

        // FIX: se valida que el paciente exista antes de crear la cita
        if (!pacienteRepository.existsById(dto.getPacienteId())) {
            throw new ResourceNotFoundException("Paciente no encontrado con ID: " + dto.getPacienteId());
        }

        // FIX: se valida que el doctor no tenga otra cita a la misma hora (doble booking)
        boolean existeConflicto = citaRepository.existsByDoctorIdAndFechaHora(dto.getDoctorId(), dto.getFechaHora());
        if (existeConflicto) {
            throw new IllegalArgumentException(
                    "El doctor ya tiene una cita programada en la fecha y hora: " + dto.getFechaHora());
        }

        Cita cita = new Cita();
        cita.setPacienteId(dto.getPacienteId());
        cita.setDoctor(doctor);
        cita.setFechaHora(dto.getFechaHora());
        cita.setMotivo(dto.getMotivo());
        cita.setEstado(dto.getEstado() != null ? dto.getEstado() : "PROGRAMADA");

        return citaRepository.save(cita);
    }

    @Transactional // FIX: rollback automatico si falla a mitad de la actualizacion
    public Cita actualizar(Long id, CitaDTO dto) {
        Cita cita = buscarPorId(id);
        cita.setFechaHora(dto.getFechaHora());
        cita.setMotivo(dto.getMotivo());
        if (dto.getEstado() != null) {
            cita.setEstado(dto.getEstado());
        }
        return citaRepository.save(cita);
    }

    @Transactional // FIX: operacion de escritura, necesita transaccion de lectura-escritura
    public void eliminar(Long id) {
        if (!citaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Cita no encontrada con ID: " + id);
        }
        citaRepository.deleteById(id);
    }

    public List<Cita> listarPorPaciente(Long pacienteId) {
        return citaRepository.findByPacienteId(pacienteId);
    }

    public List<Cita> listarPorDoctor(Long doctorId) {
        // FIX: el repositorio ya usa JOIN FETCH internamente para evitar N+1
        return citaRepository.findByDoctorId(doctorId);
    }

    public List<Cita> listarPorEstado(String estado) {
        // FIX: el repositorio ya usa JOIN FETCH internamente para evitar N+1
        return citaRepository.findCitasByEstadoOrdered(estado);
    }

    public List<Cita> listarPorRangoFechas(LocalDateTime inicio, LocalDateTime fin) {
        // FIX: se valida que inicio no sea posterior a fin
        if (inicio.isAfter(fin)) {
            throw new IllegalArgumentException("La fecha de inicio no puede ser posterior a la fecha de fin");
        }
        return citaRepository.findByFechaHoraBetween(inicio, fin);
    }
}