package com.hospital.service.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.HistoriaClinicaDTO;
import com.hospital.model.Doctor;
import com.hospital.model.HistoriaClinica;
import com.hospital.model.Paciente;
import com.hospital.repository.DoctorRepository;
import com.hospital.repository.HistoriaClinicaRepository;
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
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class HistoriaClinicaControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private HistoriaClinicaRepository historiaRepository;

    @Autowired
    private PacienteRepository pacienteRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    private Paciente paciente;
    private Doctor doctor;
    private HistoriaClinicaDTO dtoValido;

    @BeforeEach
    void setUp() {
        historiaRepository.deleteAll();
        pacienteRepository.deleteAll();
        doctorRepository.deleteAll();

        paciente = pacienteRepository.save(
                new Paciente("Camila", "Ortiz", LocalDate.of(1990, Month.JUNE, 15),
                        "camila.ortiz@example.com", "0991230000", "Quito"));

        doctor = doctorRepository.save(
                new Doctor("Andres", "Peña", "Medicina General", "andres.pena@hospital.com", "0991240000", "C-102"));

        dtoValido = new HistoriaClinicaDTO();
        dtoValido.setPacienteId(paciente.getId());
        dtoValido.setDoctorId(doctor.getId());
        dtoValido.setDiagnostico("Gripe comun");
        dtoValido.setTratamiento("Reposo e hidratacion");
        dtoValido.setObservaciones("Control en 7 dias si persisten sintomas");
    }


    @Test
    @DisplayName("POST /api/historias-clinicas - doctorId nulo (opcional) - se acepta sin doctor")
    void crear_doctorIdNulo_esValido() throws Exception {
        dtoValido.setDoctorId(null);

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.doctor").doesNotExist());
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - pacienteId nulo - retorna 400")
    void crear_pacienteIdNulo_retorna400() throws Exception {
        dtoValido.setPacienteId(null);

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.pacienteId").exists());
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - diagnostico vacio - retorna 400")
    void crear_diagnosticoVacio_retorna400() throws Exception {
        dtoValido.setDiagnostico("");

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.diagnostico").exists());
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - paciente inexistente - retorna 404")
    void crear_pacienteInexistente_retorna404() throws Exception {
        dtoValido.setPacienteId(999999L);

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Paciente no encontrado")));
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - doctor inexistente - bug: retorna 200 en vez de 404")
    void crear_doctorInexistente_bugDeteccion() throws Exception {
        dtoValido.setDoctorId(999999L);

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Doctor no encontrado")));
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - diagnostico con script embebido - bug: XSS, se guarda sin sanitizar")
    void crear_contenidoXssEnDiagnostico_bugDeteccion() throws Exception {
        String payloadXss = "<script>alert('xss')</script>";
        dtoValido.setDiagnostico(payloadXss);

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.diagnostico", is(payloadXss)));

        HistoriaClinica guardada = historiaRepository.findAll().stream()
                .filter(h -> h.getDiagnostico().equals(payloadXss))
                .findFirst()
                .orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(payloadXss, guardada.getDiagnostico());
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - diagnostico nulo - retorna 400")
    void crear_diagnosticoNulo_retorna400() throws Exception {
        dtoValido.setDiagnostico(null);

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.diagnostico").exists());
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - diagnostico excede longitud maxima - retorna 400")
    void crear_diagnosticoExcedeLongitud_retorna400() throws Exception {
        dtoValido.setDiagnostico("A".repeat(1001)); // ajustar segun el @Size real del DTO

        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/historias-clinicas - datos validos - retorna 201")
    void crear_datosValidos_retorna201() throws Exception {
        mockMvc.perform(post("/api/historias-clinicas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.diagnostico", is("Gripe comun")));
    }
    // ---------- GET /api/historias-clinicas/{id} ----------
    @Test
    @DisplayName("GET /api/historias-clinicas/{id} - id no numerico - retorna 400")
    void buscar_idNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/historias-clinicas/{id} - id negativo - retorna 404")
    void buscar_idNegativo_retorna404() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/{id}", -1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/historias-clinicas/{id} - id existente - retorna 200 con la historia")
    void buscar_idExistente_retornaHistoria() throws Exception {
        HistoriaClinica historia = historiaRepository.save(
                new HistoriaClinica(paciente, doctor, "Migrana", "Analgesicos", "Sin complicaciones"));

        mockMvc.perform(get("/api/historias-clinicas/{id}", historia.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.diagnostico", is("Migrana")));
    }

    @Test
    @DisplayName("GET /api/historias-clinicas/{id} - id inexistente - retorna 404")
    void buscar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---------- GET /api/historias-clinicas ----------

    @Test
    @DisplayName("GET /api/historias-clinicas - varias historias - retorna todas ordenadas por fecha desc (bug: sin paginacion)")
    void listar_variasHistorias_retornaOrdenDescendente() throws Exception {
        LocalDateTime fechaReferencia =
                LocalDateTime.of(2026, Month.JULY, 16, 10, 0);

        HistoriaClinica historiaAntigua =
                new HistoriaClinica(
                        paciente,
                        doctor,
                        "Diagnostico antiguo",
                        null,
                        null
                );
        historiaAntigua.setFechaCreacion(fechaReferencia.minusDays(1));

        HistoriaClinica historiaReciente =
                new HistoriaClinica(
                        paciente,
                        doctor,
                        "Diagnostico reciente",
                        null,
                        null
                );
        historiaReciente.setFechaCreacion(fechaReferencia);

        historiaRepository.saveAllAndFlush(
                List.of(historiaAntigua, historiaReciente)
        );

        mockMvc.perform(get("/api/historias-clinicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath(
                        "$[0].diagnostico",
                        is("Diagnostico reciente")
                ))
                .andExpect(jsonPath(
                        "$[1].diagnostico",
                        is("Diagnostico antiguo")
                ));
    }

    @Test
    @DisplayName("GET /api/historias-clinicas - sin historias - retorna lista vacia")
    void listar_sinHistorias_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /api/historias-clinicas/paciente/{pacienteId} ----------

    @Test
    @DisplayName("GET /api/historias-clinicas/paciente/{id} - paciente con historias - retorna lista")
    void listarPorPaciente_conHistorias_retornaLista() throws Exception {
        historiaRepository.save(new HistoriaClinica(paciente, doctor, "Chequeo anual", null, null));

        mockMvc.perform(get("/api/historias-clinicas/paciente/{pacienteId}", paciente.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/historias-clinicas/paciente/{id} - paciente sin historias - retorna lista vacia")
    void listarPorPaciente_sinHistorias_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/paciente/{pacienteId}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // ---------- GET /api/historias-clinicas/doctor/{doctorId} ----------
    @Test
    @DisplayName("GET /api/historias-clinicas/doctor/{id} - doctor sin historias - retorna lista vacia")
    void listarPorDoctor_sinHistorias_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/doctor/{doctorId}", doctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/historias-clinicas/doctor/{id} - doctor inexistente - retorna lista vacia")
    void listarPorDoctor_doctorInexistente_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/historias-clinicas/doctor/{doctorId}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }
    @Test
    @DisplayName("GET /api/historias-clinicas/doctor/{id} - doctor con historias - retorna lista")
    void listarPorDoctor_conHistorias_retornaLista() throws Exception {
        historiaRepository.save(new HistoriaClinica(paciente, doctor, "Control post-operatorio", null, null));

        mockMvc.perform(get("/api/historias-clinicas/doctor/{doctorId}", doctor.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }
}