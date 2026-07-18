package com.hospital.repository;

import com.hospital.model.Cita;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByPacienteId(Long pacienteId);

    // FIX: version con JOIN FETCH para traer el doctor en la misma consulta y evitar N+1
    @Query("SELECT c FROM Cita c JOIN FETCH c.doctor WHERE c.doctor.id = :doctorId")
    List<Cita> findByDoctorId(@Param("doctorId") Long doctorId);

    List<Cita> findByEstado(String estado);

    List<Cita> findByFechaHoraBetween(LocalDateTime inicio, LocalDateTime fin);

    // FIX: se agrega JOIN FETCH para traer el doctor en la misma consulta y evitar N+1
    @Query("SELECT c FROM Cita c JOIN FETCH c.doctor WHERE c.estado = :estado ORDER BY c.fechaHora")
    List<Cita> findCitasByEstadoOrdered(@Param("estado") String estado);

    // FIX: metodo nuevo para validar conflicto de horario (doble booking) antes de crear una cita
    boolean existsByDoctorIdAndFechaHora(Long doctorId, LocalDateTime fechaHora);

    boolean existsByDoctorIdAndEstado(Long doctorId, String estado);
}