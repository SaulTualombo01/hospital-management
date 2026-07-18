package com.hospital.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public class DoctorDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El nombre solo puede contener letras y espacios") // FIX: rechaza numeros
    private String nombre;

    @NotBlank(message = "El apellido es obligatorio")
    @Pattern(regexp = "^[\\p{L} ]+$", message = "El apellido solo puede contener letras y espacios") // FIX: rechaza numeros
    private String apellido;

    @NotBlank(message = "La especialidad es obligatoria") // FIX: faltaba esta anotacion
    private String especialidad;

    @NotBlank(message = "El email es obligatorio") // FIX: faltaba, @Email solo no rechaza vacio/nulo
    @Email(message = "El email debe ser valido")
    private String email;

    @NotBlank(message = "El telefono es obligatorio") // FIX: faltaba validacion
    @Pattern(regexp = "^[0-9]{7,10}$", message = "El telefono debe contener solo numeros (7 a 10 digitos)") // FIX
    private String telefono;

    private String consultorio;

    public DoctorDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getApellido() { return apellido; }
    public void setApellido(String apellido) { this.apellido = apellido; }

    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefono() { return telefono; }
    public void setTelefono(String telefono) { this.telefono = telefono; }

    public String getConsultorio() { return consultorio; }
    public void setConsultorio(String consultorio) { this.consultorio = consultorio; }
}