package com.hospital.repository;

import com.hospital.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByEspecialidadContainingIgnoreCase(String especialidad);

    List<Doctor> findByApellidoContainingIgnoreCase(String apellido);

    // NOTA: query derivada, coincidencia exacta de nombre y apellido.
    // El comentario original indicaba que el nombre del metodo puede confundir,
    // ya que no hace busqueda parcial como "Containing". Se documenta como
    // hallazgo de nomenclatura, no como vulnerabilidad de seguridad.
    List<Doctor> findByNombreAndApellido(String nombre, String apellido);

    // FIX: metodo nuevo para validar email duplicado antes de crear/actualizar un doctor
    boolean existsByEmail(String email);
}