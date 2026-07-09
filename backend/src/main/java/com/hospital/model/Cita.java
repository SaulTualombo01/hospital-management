package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Entity
@Table(name = "citas")
public class Cita {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Mantenemos el campo que tus otras clases esperan
    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    // Añadimos la relación opcional para satisfacer la Foreign Key de la BD
    // insertable = false, updatable = false permite que la relación exista
    // sin romper tu lógica actual de usar pacienteId
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", insertable = false, updatable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id", nullable = false)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
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

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}