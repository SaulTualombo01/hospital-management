package com.hospital.service;

import com.hospital.dto.PacienteDTO;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Paciente;
import com.hospital.repository.PacienteRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PacienteService {

    private final PacienteRepository pacienteRepository;

    public PacienteService(PacienteRepository pacienteRepository) {
        this.pacienteRepository = pacienteRepository;
    }

    public List<Paciente> listarTodos() {
        return pacienteRepository.findAll();
    }

    public Paciente buscarPorId(Long id) {
        // BUG INTENCIONAL: No maneja correctamente el caso de ID negativo
        // Ademas, orElseThrow no esta importado/definido correctamente en el repository
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con ID: " + id));
    }

    public Paciente crear(PacienteDTO dto) {
        // FIX: se valida que el email no este ya registrado
        if (pacienteRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Ya existe un paciente registrado con el email: " + dto.getEmail());
        }

        Paciente paciente = toEntity(dto);
        return pacienteRepository.save(paciente);
    }

    public Paciente actualizar(Long id, PacienteDTO dto) {
        Paciente paciente = buscarPorId(id);
        // BUG INTENCIONAL: actualiza todos los campos sin validar si vienen nulos
        paciente.setNombre(dto.getNombre());
        paciente.setApellido(dto.getApellido());
        paciente.setFechaNacimiento(dto.getFechaNacimiento());
        paciente.setEmail(dto.getEmail());
        paciente.setTelefono(dto.getTelefono());
        paciente.setDireccion(dto.getDireccion());
        if (dto.getActivo() != null) {
            paciente.setActivo(dto.getActivo());
        }
        return pacienteRepository.save(paciente);
    }

    public void eliminar(Long id) {
        // BUG INTENCIONAL: Eliminacion fisica sin verificar dependencias (citas, historias)
        // Deberia ser borrado logico o verificar FK constraints
        Paciente paciente = buscarPorId(id);
        pacienteRepository.delete(paciente);
    }

    public List<Paciente> buscarPorNombre(String nombre) {
        // BUG INTENCIONAL: si nombre es null, el metodo falla con NullPointerException
        // Ademas, usa una query nativa que es innecesaria
        return pacienteRepository.buscarPorNombre(nombre);
    }

    public Paciente buscarPorEmail(String email) {
        // FIX: se valida que el email no sea nulo/vacio antes de consultar
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("El email no puede estar vacio");
        }

        // FIX: se maneja explicitamente el caso de ausencia de resultado con Optional
        return pacienteRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado con email: " + email));
    }

    public double calcularEdadPromedio() {
        List<Paciente> pacientes = pacienteRepository.findAll();
        // BUG INTENCIONAL: division por cero si no hay pacientes
        long suma = 0;
        for (Paciente p : pacientes) {
            if (p.getFechaNacimiento() != null) {
                suma += java.time.Period.between(p.getFechaNacimiento(), java.time.LocalDate.now()).getYears();
            }
        }
        return (double) suma / pacientes.size(); // BUG: division by zero si lista vacia
    }

    private Paciente toEntity(PacienteDTO dto) {
        Paciente p = new Paciente();
        p.setNombre(dto.getNombre());
        p.setApellido(dto.getApellido());
        p.setFechaNacimiento(dto.getFechaNacimiento());
        p.setEmail(dto.getEmail());
        p.setTelefono(dto.getTelefono());
        p.setDireccion(dto.getDireccion());
        p.setActivo(dto.getActivo() != null ? dto.getActivo() : true);
        return p;
    }
}
