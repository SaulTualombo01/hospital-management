package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // BUG INTENCIONAL: relacion sin @ManyToOne con FK explícita
    // Solo se mapea como columna, permitiendo IDs de pacientes inexistentes
    /* =========================================================================
     * BUG 1: EL PROBLEMA DEL "NÚMERO VS. PACIENTE REAL"
     * =========================================================================
     * Aquí el sistema trataba al paciente simplemente como un número suelto (Long).
     * Como el código seguía enviando solo un número, la base de
     * datos se confundía y bloqueaba la operación. Esto hacía que el servidor
     * se rompiera por dentro (Error 500) y la tabla del usuario se quedara vacía.
     * ========================================================================= */
    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    // Esta si tiene la relacion correcta
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @Column(length = 255)
    private String motivo;

    @Column(length = 20)
    private String estado = "PROGRAMADA";

    public Cita() {}

    public Cita(Long pacienteId, Doctor doctor, LocalDateTime fechaHora,
                String motivo, String estado) {
        this.pacienteId = pacienteId;
        this.doctor = doctor;
        this.fechaHora = fechaHora;
        this.motivo = motivo;
        this.estado = estado;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}