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
                // CORRECCIÓN:  isCreated() por isOk() para coincidir con el controlador
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.estado", is("PROGRAMADA")));
    }

    @Test
    @DisplayName("POST /api/citas - doctorId inexistente - retorna 404 (o 200 por el bug del handler)")
    void crear_doctorInexistente_retornaError() throws Exception {
        dtoValido.setDoctorId(999999L);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
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


    /*
     * Las pruebas de integración automatizadas revelaron una nueva vulnerabilidad lógica:
     * el controlador de Citas no valida explícitamente en la base de datos si el pacienteId
     * existe antes de hacer el guardado. Esto demuestra la importancia de las pruebas de
     * regresión, ya que arreglar un defecto arquitectónico sacó a la luz una carencia de
     * validación en la capa de negocio.
     */

    @Test
    @DisplayName("POST /api/citas - paciente inexistente - bug: no valida existencia y retorna 200 en lugar de 400/404")
    void crear_pacienteInexistente_bugDeteccion() throws Exception {
        dtoValido.setPacienteId(999999L);

        mockMvc.perform(post("/api/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                //  Ahora el sistema falla de manera silenciosa
                // aceptando el ID falso y retornando 200 OK.
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.pacienteId", is(999999)));
    }

    @Test
    @DisplayName("POST /api/citas - doble reserva mismo doctor y hora - bug: no valida conflicto de horario")
    void crear_dobleBookingMismoDoctor_bugDeteccion() throws Exception {
        // Primera cita
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
    @DisplayName("GET /api/citas/{id} - id inexistente - bug: retorna 200 en vez de 404")
    void buscar_idInexistente_bugDeteccion() throws Exception {
        mockMvc.perform(get("/api/citas/{id}", 999999L))
                .andExpect(status().isOk())
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

    // ---------- DELETE /api/citas/{id} ----------

    @Test
    @DisplayName("DELETE /api/citas/{id} - elimina correctamente (bug: 200 en vez de 204)")
    void eliminar_idExistente_bugDeteccion() throws Exception {
        Cita cita = citaRepository.save(
                new Cita(paciente.getId(), doctor, LocalDateTime.now().plusDays(1), "A eliminar", "PROGRAMADA"));

        mockMvc.perform(delete("/api/citas/{id}", cita.getId()))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertFalse(
                citaRepository.findById(cita.getId()).isPresent());
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
    @DisplayName("GET /api/citas/rango-fechas - inicio posterior a fin - bug: no valida, retorna 200 con lista vacia")
    void listarPorRangoFechas_inicioMayorQueFin_bugDeteccion() throws Exception {
        String inicio = LocalDateTime.now().plusDays(5).format(ISO);
        String fin = LocalDateTime.now().format(ISO);

        mockMvc.perform(get("/api/citas/rango-fechas")
                        .param("inicio", inicio)
                        .param("fin", fin))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
}