package com.hospital.service;

import com.hospital.dto.DoctorDTO;
import com.hospital.exception.ResourceNotFoundException;
import com.hospital.model.Doctor;
import com.hospital.repository.CitaRepository;
import com.hospital.repository.DoctorRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@Transactional(readOnly = true) // FIX: se agrega @Transactional a nivel de clase, solo lectura por defecto
public class DoctorService {

    private final DoctorRepository doctorRepository;
    private final CitaRepository citaRepository; // FIX: se necesita para validar citas activas antes de eliminar

    public DoctorService(DoctorRepository doctorRepository, CitaRepository citaRepository) {
        this.doctorRepository = doctorRepository;
        this.citaRepository = citaRepository;
    }

    public List<Doctor> listarTodos() {
        return doctorRepository.findAll();
    }

    public Doctor buscarPorId(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado con ID: " + id));
    }

    @Transactional // FIX: operacion de escritura, con rollback si falla
    public Doctor crear(DoctorDTO dto) {
        // FIX: se valida que el email no este ya registrado por otro doctor
        if (doctorRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un doctor registrado con el email: " + dto.getEmail());
        }

        Doctor doctor = toEntity(dto);
        return doctorRepository.save(doctor);
    }

    @Transactional // FIX: operacion de escritura, con rollback si falla
    public Doctor actualizar(Long id, DoctorDTO dto) {
        Doctor doctor = buscarPorId(id);

        // FIX: se valida que el nuevo email no pertenezca a OTRO doctor distinto
        if (!doctor.getEmail().equals(dto.getEmail()) && doctorRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Ya existe un doctor registrado con el email: " + dto.getEmail());
        }

        doctor.setNombre(dto.getNombre());
        doctor.setApellido(dto.getApellido());
        doctor.setEspecialidad(dto.getEspecialidad());
        doctor.setEmail(dto.getEmail());
        doctor.setTelefono(dto.getTelefono());
        doctor.setConsultorio(dto.getConsultorio());
        return doctorRepository.save(doctor);
    }

    @Transactional // FIX: operacion de escritura, con rollback si falla
    public void eliminar(Long id) {
        // FIX: se verifica existencia antes de eliminar, para poder devolver 404 si no existe
        if (!doctorRepository.existsById(id)) {
            throw new ResourceNotFoundException("Doctor no encontrado con ID: " + id);
        }

        // FIX: se valida que el doctor no tenga citas activas antes de eliminarlo
        boolean tieneCitasActivas = citaRepository.existsByDoctorIdAndEstado(id, "PROGRAMADA");
        if (tieneCitasActivas) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el doctor porque tiene citas activas programadas");
        }

        doctorRepository.deleteById(id);
    }

    // FIX: se elimina el metodo inseguro que concatenaba strings (SQL Injection)
    // y se usa siempre la version segura con Spring Data JPA (query parametrizada)
    public List<Doctor> buscarPorEspecialidadInsegura(String especialidad) {
        return doctorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    public List<Doctor> buscarPorEspecialidad(String especialidad) {
        return doctorRepository.findByEspecialidadContainingIgnoreCase(especialidad);
    }

    // FIX: se valida que nombre y apellido no esten vacios antes de consultar
    public List<Doctor> buscarPorNombreCompleto(String nombre, String apellido) {
        if (!StringUtils.hasText(nombre) || !StringUtils.hasText(apellido)) {
            throw new IllegalArgumentException("El nombre y el apellido son obligatorios para la busqueda");
        }
        return doctorRepository.findByNombreAndApellido(nombre, apellido);
    }

    private Doctor toEntity(DoctorDTO dto) {
        Doctor d = new Doctor();
        d.setNombre(dto.getNombre());
        d.setApellido(dto.getApellido());
        d.setEspecialidad(dto.getEspecialidad());
        d.setEmail(dto.getEmail());
        d.setTelefono(dto.getTelefono());
        d.setConsultorio(dto.getConsultorio());
        return d;
    }
}