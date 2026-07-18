package com.hospital.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class HistoriaClinicaDTO {

    private Long id;

    @NotNull(message = "El ID del paciente es obligatorio")
    @Positive(message = "El ID del paciente debe ser un valor positivo") // FIX: faltaba esta anotacion
    private Long pacienteId;

    @Positive(message = "El ID del doctor debe ser un valor positivo") // FIX: faltaba esta anotacion
    private Long doctorId;

    private LocalDateTime fechaCreacion;

    @NotBlank(message = "El diagnostico es obligatorio")
    @Size(max = 1000, message = "El diagnostico no puede exceder 1000 caracteres") // FIX: faltaba esta anotacion
    private String diagnostico;

    @Size(max = 1000, message = "El tratamiento no puede exceder 1000 caracteres") // FIX: faltaba esta anotacion
    private String tratamiento;

    @Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres") // FIX: faltaba esta anotacion
    private String observaciones;

    public HistoriaClinicaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(LocalDateTime fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public String getDiagnostico() { return diagnostico; }
    public void setDiagnostico(String diagnostico) { this.diagnostico = diagnostico; }

    public String getTratamiento() { return tratamiento; }
    public void setTratamiento(String tratamiento) { this.tratamiento = tratamiento; }

    public String getObservaciones() { return observaciones; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}