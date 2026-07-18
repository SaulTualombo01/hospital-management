package com.hospital.repository;

import com.hospital.model.Paciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    // BUG INTENCIONAL: consulta vulnerable a SQL Injection (uso de concatenacion)
    @Query(value = "SELECT * FROM pacientes WHERE nombre LIKE '%' || :nombre || '%'", nativeQuery = true)
    List<Paciente> buscarPorNombre(@Param("nombre") String nombre);

    List<Paciente> findByApellidoContainingIgnoreCase(String apellido);

    List<Paciente> findByActivoTrue();

    // FIX: se envuelve en Optional para forzar manejo expl[icito de ausencia de resultado
    Optional<Paciente> findByEmail(String email);
}