package com.hospital.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "historias_clinicas")
public class HistoriaClinica {
    /* =========================================================================
     * BUG 2: EL PROBLEMA DEL "DATO FANTASMA"
     * =========================================================================
     * Para ahorrar memoria, el servidor no carga toda la información del paciente
     * de golpe, sino que crea un dato "fantasma" temporal (FetchType.LAZY).
     * El problema es que, cuando el servidor intenta empaquetar esa información
     * en texto (JSON) para enviarla al navegador, el empaquetador choca con ese
     * fantasma, no sabe cómo leerlo y hace colapsar la aplicación. Se soluciona
     * añadiendo una etiqueta (@JsonIgnoreProperties) que le dice al empaquetador
     * que simplemente ignore esas partes confusas.
     * ========================================================================= */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paciente_id", nullable = false)
    private Paciente paciente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "doctor_id")
    private Doctor doctor;

    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();

    @Column(nullable = false, columnDefinition = "TEXT")
    private String diagnostico;

    @Column(columnDefinition = "TEXT")
    private String tratamiento;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    public HistoriaClinica() {}

    public HistoriaClinica(Paciente paciente, Doctor doctor, String diagnostico,
                           String tratamiento, String observaciones) {
        this.paciente = paciente;
        this.doctor = doctor;
        this.diagnostico = diagnostico;
        this.tratamiento = tratamiento;
        this.observaciones = observaciones;
        this.fechaCreacion = LocalDateTime.now();
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Paciente getPaciente() { return paciente; }
    public void setPaciente(Paciente paciente) { this.paciente = paciente; }

    public Doctor getDoctor() { return doctor; }
    public void setDoctor(Doctor doctor) { this.doctor = doctor; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}