package com.hospital.service.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.CitaDTO;
import com.hospital.model.Cita;
import com.hospital.model.Doctor;
import com.hospital.model.Paciente;
import com.hospital.repository.CitaRepository;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.PacienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CitaControllerIntegrationTest {

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CitaRepository citaRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    private Doctor doctor;
    private Paciente paciente;
    private CitaDTO dtoValido;

    @BeforeEach
    void setUp() {
        citaRepository.deleteAll();
        doctorRepository.deleteAll();
        pacienteRepository.deleteAll();

        doctor = doctorRepository.save(
                new Doctor("Felipe", "Rios", "Cardiologia", "felipe.rios@hospital.com", "0991230000", "C-101"));

        paciente = pacienteRepository.save(
                new Paciente("Sofia", "Mora", LocalDate.of(1993, Month.APRIL, 12),
                        "sofia.mora@example.com", "0991112222", "Quito"));

        dtoValido = new CitaDTO();
        dtoValido.setPacienteId(paciente.getId());
        dtoValido.setDoctorId(doctor.getId());
        dtoValido.setFechaHora(LocalDateTime.now().plusDays(2));
        dtoValido.setMotivo("Control de rutina");
        dtoValido.setEstado("PROGRAMADA");
    }

    // ---------- POST /api/citas ----------
    @Test
    @DisplayName("POST /api/citas - datos validos - retorna 200 (Éxito genérico en lugar de 201 Created)")
    void crear_datosValidos_retorna201() throws Exception {
        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado", is("PROGRAMADA")));
    }

    @Test
    @DisplayName("POST /api/citas - doctorId inexistente - retorna 404")
    void crear_doctorInexistente_retorna404() throws Exception {
        dtoValido.setDoctorId(999999L);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Doctor no encontrado")));
    }

    @Test
    @DisplayName("POST /api/citas - pacienteId nulo - retorna 400")
    void crear_pacienteIdNulo_retorna400() throws Exception {
        dtoValido.setPacienteId(null);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pacienteId").exists());
    }

    @Test
    @DisplayName("POST /api/citas - fecha pasada (invalida) - retorna 400")
    void crear_fechaPasada_retorna400() throws Exception {
        dtoValido.setFechaHora(LocalDateTime.now().minusDays(1));

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fechaHora").exists());
    }

    @Test
    @DisplayName("POST /api/citas - fecha actual (valor limite) - bug: @Future la rechaza aunque deberia aceptarse")
    void crear_fechaActual_bugDeteccion() throws Exception {
        dtoValido.setFechaHora(LocalDateTime.now());

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }


    @Test
    @DisplayName("POST /api/citas - paciente inexistente - retorna 404")
    void crear_pacienteInexistente_retorna404() throws Exception {
        dtoValido.setPacienteId(999999L);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Paciente no encontrado")));
    }

    @Test
    @DisplayName("POST /api/citas - doble reserva mismo doctor y hora - bug: no valida conflicto de horario")
    void crear_dobleBookingMismoDoctor_bugDeteccion() throws Exception {
        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk());

        CitaDTO segunda = new CitaDTO();
        segunda.setPacienteId(paciente.getId());
        segunda.setDoctorId(doctor.getId());
        segunda.setFechaHora(dtoValido.getFechaHora());
        segunda.setMotivo("Otra consulta");
        segunda.setEstado("PROGRAMADA");

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(segunda)))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertEquals(
                2, citaRepository.findByDoctorId(doctor.getId()).size());
    }
    @Test
    @DisplayName("POST /api/citas - doctorId nulo - retorna 400")
    void crear_doctorIdNulo_retorna400() throws Exception {
        dtoValido.setDoctorId(null);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.doctorId").exists());
    }

    @Test
    @DisplayName("POST /api/citas - pacienteId negativo - retorna 400")
    void crear_pacienteIdNegativo_retorna400() throws Exception {
        dtoValido.setPacienteId(-1L);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/citas - motivo vacio - retorna 400")
    void crear_motivoVacio_retorna400() throws Exception {
        dtoValido.setMotivo("");

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.motivo").exists());
    }

    @Test
    @DisplayName("POST /api/citas - motivo nulo - retorna 400")
    void crear_motivoNulo_retorna400() throws Exception {
        dtoValido.setMotivo(null);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.motivo").exists());
    }

    @Test
    @DisplayName("POST /api/citas - motivo excede longitud maxima - retorna 400")
    void crear_motivoExcedeLongitud_retorna400() throws Exception {
        dtoValido.setMotivo("A".repeat(300)); // ajustar segun el @Size real del DTO

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/citas - fechaHora nula - retorna 400")
    void crear_fechaHoraNula_retorna400() throws Exception {
        dtoValido.setFechaHora(null);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fechaHora").exists());
    }

    // ---------- GET /api/citas/{id} ----------

    @Test
    @DisplayName("GET /api/citas/{id} - id existente - retorna 200 con la cita")
    void buscar_idExistente_retornaCita() throws Exception {
        Cita cita = citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "Chequeo", "PROGRAMADA"));

        mockMvc.perform(get("/api/citas/{id}", cita.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo", is("Chequeo")));
    }

    @Test
    @DisplayName("GET /api/citas/{id} - id inexistente - retorna 404")
    void buscar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/citas/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/citas/{id} - id no numerico - retorna 400")
    void buscar_idNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get("/api/citas/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/citas/{id} - id negativo - retorna 404")
    void buscar_idNegativo_retorna404() throws Exception {
        mockMvc.perform(get("/api/citas/{id}", -1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---------- PUT /api/citas/{id} ----------

    @Test
    @DisplayName("PUT /api/citas/{id} - actualizacion valida - retorna cita actualizada")
    void actualizar_datosValidos_retornaActualizada() throws Exception {
        Cita cita = citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "Motivo original", "PROGRAMADA"));

        CitaDTO actualizacion = new CitaDTO();
        actualizacion.setPacienteId(paciente.getId());
        actualizacion.setDoctorId(doctor.getId());
        actualizacion.setFechaHora(LocalDateTime.now().plusDays(3));
        actualizacion.setMotivo("Motivo actualizado");
        actualizacion.setEstado("CONFIRMADA");

        mockMvc.perform(put("/api/citas/{id}", cita.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.motivo", is("Motivo actualizado")))
                .andExpect(jsonPath("$.estado", is("CONFIRMADA")));
    }

    @Test
    @DisplayName("PUT /api/citas/{id} - id inexistente - retorna 404")
    void actualizar_idInexistente_retorna404() throws Exception {
        CitaDTO actualizacion = new CitaDTO();
        actualizacion.setPacienteId(paciente.getId());
        actualizacion.setDoctorId(doctor.getId());
        actualizacion.setFechaHora(LocalDateTime.now().plusDays(3));
        actualizacion.setMotivo("Motivo actualizado");
        actualizacion.setEstado("CONFIRMADA");

        mockMvc.perform(put("/api/citas/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/citas/{id} - pacienteId nulo en actualizacion - retorna 400")
    void actualizar_pacienteIdNulo_retorna400() throws Exception {
        Cita cita = citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "Motivo original", "PROGRAMADA"));

        CitaDTO actualizacion = new CitaDTO();
        actualizacion.setPacienteId(null);
        actualizacion.setDoctorId(doctor.getId());
        actualizacion.setFechaHora(LocalDateTime.now().plusDays(3));
        actualizacion.setMotivo("Motivo actualizado");
        actualizacion.setEstado("CONFIRMADA");

        mockMvc.perform(put("/api/citas/{id}", cita.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/citas/{id} - fecha pasada en actualizacion - retorna 400")
    void actualizar_fechaPasada_retorna400() throws Exception {
        Cita cita = citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "Motivo original", "PROGRAMADA"));

        CitaDTO actualizacion = new CitaDTO();
        actualizacion.setPacienteId(paciente.getId());
        actualizacion.setDoctorId(doctor.getId());
        actualizacion.setFechaHora(LocalDateTime.now().minusDays(1));
        actualizacion.setMotivo("Motivo actualizado");
        actualizacion.setEstado("CONFIRMADA");

        mockMvc.perform(put("/api/citas/{id}", cita.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /api/citas/{id} ----------

    @Test
    @DisplayName("DELETE /api/citas/{id} - elimina correctamente - retorna 204")
    void eliminar_idExistente_retorna204() throws Exception {
        Cita cita = citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "A eliminar", "PROGRAMADA"));

        mockMvc.perform(delete("/api/citas/{id}", cita.getId()))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                citaRepository.findById(cita.getId()).isPresent());
    }
    @Test
    @DisplayName("DELETE /api/citas/{id} - id inexistente - retorna 404")
    void eliminar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(delete("/api/citas/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    // ---------- GET /api/citas/paciente/{pacienteId} ----------

    @Test
    @DisplayName("GET /api/citas/paciente/{id} - paciente con citas - retorna lista")
    void listarPorPaciente_conCitas_retornaLista() throws Exception {
        citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "Motivo", "PROGRAMADA"));

        mockMvc.perform(get("/api/citas/paciente/{pacienteId}", paciente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
    @Test
    @DisplayName("GET /api/citas/paciente/{id} - paciente sin citas - retorna lista vacia")
    void listarPorPaciente_sinCitas_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/citas/paciente/{pacienteId}", paciente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/citas/paciente/{id} - paciente inexistente - retorna lista vacia")
    void listarPorPaciente_pacienteInexistente_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/citas/paciente/{pacienteId}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    // ---------- GET /api/citas/doctor/{doctorId} ----------

    @Test
    @DisplayName("GET /api/citas/doctor/{id} - doctor con citas - retorna lista")
    void listarPorDoctor_conCitas_retornaLista() throws Exception {
        citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "Motivo", "PROGRAMADA"));

        mockMvc.perform(get("/api/citas/doctor/{doctorId}", doctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
    @Test
    @DisplayName("GET /api/citas/doctor/{id} - doctor sin citas - retorna lista vacia")
    void listarPorDoctor_sinCitas_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/citas/doctor/{doctorId}", doctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/citas/doctor/{id} - doctor inexistente - retorna lista vacia")
    void listarPorDoctor_doctorInexistente_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/citas/doctor/{doctorId}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    // ---------- GET /api/citas/estado/{estado} ----------

    @Test
    @DisplayName("GET /api/citas/estado/{estado} - estado existente - retorna citas ordenadas por fecha")
    void listarPorEstado_estadoExistente_retornaListaOrdenada() throws Exception {
        citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(2), "Segunda", "CONFIRMADA"));
        citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "Primera", "CONFIRMADA"));

        mockMvc.perform(get("/api/citas/estado/{estado}", "CONFIRMADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].motivo", is("Primera")));
    }
    @Test
    @DisplayName("GET /api/citas/estado/{estado} - estado no reconocido - retorna lista vacia")
    void listarPorEstado_estadoNoReconocido_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/citas/estado/{estado}", "INVENTADO"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    @Test
    @DisplayName("GET /api/citas/estado/{estado} - estado sin citas - retorna lista vacia")
    void listarPorEstado_sinCoincidencias_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/citas/estado/{estado}", "CANCELADA"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /api/citas/rango-fechas ----------

    @Test
    @DisplayName("GET /api/citas/rango-fechas - rango valido - retorna citas dentro del rango")
    void listarPorRangoFechas_rangoValido_retornaCoincidencias() throws Exception {
        citaRepository.save(new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1),
                "Dentro del rango", "PROGRAMADA"));
        citaRepository.save(new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(10),
                "Fuera del rango", "PROGRAMADA"));

        String inicio = LocalDateTime.now().format(ISO);
        String fin = LocalDateTime.now().plusDays(3).format(ISO);

        mockMvc.perform(get("/api/citas/rango-fechas")
                        .param("inicio", inicio)
                        .param("fin", fin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].motivo", is("Dentro del rango")));
    }
    @Test
    @DisplayName("GET /api/citas/rango-fechas - fechas iguales - retorna 200 con lista vacia")
    void listarPorRangoFechas_fechasIguales_retornaListaVacia() throws Exception {
        String fecha = LocalDateTime.now().format(ISO);

        mockMvc.perform(get("/api/citas/rango-fechas")
                        .param("inicio", fecha)
                        .param("fin", fecha))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/citas/rango-fechas - formato de fecha invalido - retorna 400")
    void listarPorRangoFechas_formatoInvalido_retorna400() throws Exception {
        mockMvc.perform(get("/api/citas/rango-fechas")
                        .param("inicio", "no-es-una-fecha")
                        .param("fin", "tampoco"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/citas/rango-fechas - parametro faltante - retorna 400")
    void listarPorRangoFechas_parametroFaltante_retorna400() throws Exception {
        String inicio = LocalDateTime.now().format(ISO);

        mockMvc.perform(get("/api/citas/rango-fechas")
                        .param("inicio", inicio))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/citas/rango-fechas - inicio posterior a fin - retorna 400")
    void listarPorRangoFechas_inicioMayorQueFin_retorna400() throws Exception {
        String inicio = LocalDateTime.now().plusDays(5).format(ISO);
        String fin = LocalDateTime.now().format(ISO);

        mockMvc.perform(get("/api/citas/rango-fechas")
                        .param("inicio", inicio)
                        .param("fin", fin))
                .andExpect(status().isBadRequest());
    }
}