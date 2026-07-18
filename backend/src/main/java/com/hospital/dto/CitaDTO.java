package com.hospital.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

public class CitaDTO {

    private Long id;

    @NotNull(message = "El ID del paciente es obligatorio")
    @Positive(message = "El ID del paciente debe ser un valor positivo") // FIX: faltaba esta anotacion
    private Long pacienteId;

    @NotNull(message = "El ID del doctor es obligatorio")
    @Positive(message = "El ID del doctor debe ser un valor positivo") // FIX: faltaba esta anotacion
    private Long doctorId;

    @NotNull(message = "La fecha y hora es obligatoria")
    @FutureOrPresent(message = "La fecha no puede ser anterior al momento actual") // FIX: antes era @Future
    private LocalDateTime fechaHora;

    @NotBlank(message = "El motivo es obligatorio") // FIX: faltaba esta anotacion
    @Size(max = 255, message = "El motivo no puede exceder 255 caracteres") // FIX: faltaba esta anotacion
    private String motivo;

    private String estado = "PROGRAMADA";

    public CitaDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public LocalDateTime getFechaHora() { return fechaHora; }
    public void setFechaHora(LocalDateTime fechaHora) { this.fechaHora = fechaHora; }

    public String getMotivo() { return motivo; }
    public void setMotivo(String motivo) { this.motivo = motivo; }

    public String getEstado() { return estado; }
    public void setEstado(String estado) { this.estado = estado; }
}