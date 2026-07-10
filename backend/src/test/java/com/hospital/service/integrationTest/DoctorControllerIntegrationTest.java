package com.hospital.service.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.DoctorDTO;
import com.hospital.model.Doctor;
import com.hospital.repository.DoctorRepository;
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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class DoctorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private DoctorRepository doctorRepository;

    private DoctorDTO dtoValido;

    @BeforeEach
    void setUp() {
        doctorRepository.deleteAll();

        dtoValido = new DoctorDTO();
        dtoValido.setNombre("Jorge");
        dtoValido.setApellido("Suarez");
        dtoValido.setEspecialidad("Cardiologia");
        dtoValido.setEmail("jorge.suarez@hospital.com");
        dtoValido.setTelefono("0987001122");
        dtoValido.setConsultorio("C-101");
    }

    // ---------- POST /api/doctores ----------

    @Test
    @DisplayName("POST /api/doctores - nombre en blanco - retorna 400")
    void crear_nombreVacio_retorna400() throws Exception {
        dtoValido.setNombre("   ");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists());
    }

    @Test
    @DisplayName("POST /api/doctores - apellido en blanco - retorna 400")
    void crear_apellidoVacio_retorna400() throws Exception {
        dtoValido.setApellido("");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.apellido").exists());
    }

    @Test
    @DisplayName("POST /api/doctores - especialidad vacia - bug: no deberia pasar pero DTO no valida (falta @NotBlank)")
    void crear_especialidadVacia_bugDeteccion() throws Exception {
        dtoValido.setEspecialidad("");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk()) // sin 400 pese a que el negocio la requiere
                .andExpect(jsonPath("$.especialidad", is("")));
    }

    @Test
    @DisplayName("POST /api/doctores - email invalido - retorna 400")
    void crear_emailInvalido_retorna400() throws Exception {
        dtoValido.setEmail("correo-no-valido");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    // ---------- GET /api/doctores/{id} ----------

    @Test
    @DisplayName("GET /api/doctores/{id} - id existente - retorna 200 con datos")
    void buscar_idExistente_retornaDoctor() throws Exception {
        Doctor guardado = doctorRepository.save(
                new Doctor("Elena", "Vega", "Pediatria", "elena.vega@hospital.com", "0991230000", "C-202"));

        mockMvc.perform(get("/api/doctores/{id}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Elena")))
                .andExpect(jsonPath("$.especialidad", is("Pediatria")));
    }

    @Test
    @DisplayName("GET /api/doctores/{id} - id inexistente - bug: retorna 200 en vez de 404")
    void buscar_idInexistente_bugDeteccion() throws Exception {
        mockMvc.perform(get("/api/doctores/{id}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---------- PUT /api/doctores/{id} ----------

    @Test
    @DisplayName("PUT /api/doctores/{id} - actualizacion valida - retorna datos actualizados")
    void actualizar_datosValidos_retornaActualizado() throws Exception {
        Doctor guardado = doctorRepository.save(
                new Doctor("Marco", "Diaz", "Traumatologia", "marco.diaz@hospital.com", "0991239999", "C-303"));

        DoctorDTO actualizacion = new DoctorDTO();
        actualizacion.setNombre("Marco Antonio");
        actualizacion.setApellido("Diaz");
        actualizacion.setEspecialidad("Traumatologia");
        actualizacion.setEmail("marco.antonio@hospital.com");
        actualizacion.setTelefono("0991239999");
        actualizacion.setConsultorio("C-303");

        mockMvc.perform(put("/api/doctores/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Marco Antonio")));
    }

    // ---------- DELETE /api/doctores/{id} ----------

    @Test
    @DisplayName("DELETE /api/doctores/{id} - elimina correctamente (bug: 200 en vez de 204)")
    void eliminar_idExistente_bugDeteccion() throws Exception {
        Doctor guardado = doctorRepository.save(
                new Doctor("Sara", "Nunez", "Dermatologia", "sara.nunez@hospital.com", "0991110000", "C-404"));

        mockMvc.perform(delete("/api/doctores/{id}", guardado.getId()))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertFalse(
                doctorRepository.findById(guardado.getId()).isPresent());
    }

    // ---------- GET /api/doctores/buscar-especialidad (vulnerable a SQL Injection) ----------

    @Test
    @DisplayName("GET /api/doctores/buscar-especialidad?q= - busqueda normal - retorna coincidencias")
    void buscarPorEspecialidad_valorNormal_retornaCoincidencias() throws Exception {
        doctorRepository.save(new Doctor("Diego", "Leon", "Neurologia", "diego.leon@hospital.com", "0991112222", "C-505"));

        mockMvc.perform(get("/api/doctores/buscar-especialidad").param("q", "Neuro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-especialidad?q= - payload de SQL Injection - documenta vulnerabilidad (OWASP)")
    void buscarPorEspecialidad_payloadSqlInjection_bugDeteccion() throws Exception {
        doctorRepository.save(new Doctor("Nora", "Cabrera", "Oncologia", "nora.cabrera@hospital.com", "0991113333", "C-606"));
        doctorRepository.save(new Doctor("Marta", "Ibarra", "Ginecologia", "marta.ibarra@hospital.com", "0991114455", "C-707"));

        String payload = "%' OR '1'='1' -- ";

        mockMvc.perform(get("/api/doctores/buscar-especialidad").param("q", payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(2))));

    }

    // ---------- GET /api/doctores/buscar-nombre ----------

    @Test
    @DisplayName("GET /api/doctores/buscar-nombre - nombre y apellido exactos - retorna coincidencia")
    void buscarPorNombreCompleto_datosExactos_retornaCoincidencia() throws Exception {
        doctorRepository.save(new Doctor("Ricardo", "Paz", "Urologia", "ricardo.paz@hospital.com", "0991114444", "C-707"));

        mockMvc.perform(get("/api/doctores/buscar-nombre")
                        .param("nombre", "Ricardo")
                        .param("apellido", "Paz"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-nombre - parametros vacios - bug: sin validacion, retorna 200 con lista vacia")
    void buscarPorNombreCompleto_parametrosVacios_bugDeteccion() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-nombre")
                        .param("nombre", "")
                        .param("apellido", ""))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-nombre - falta parametro obligatorio - bug: 500 en vez de 400")
    void buscarPorNombreCompleto_faltaParametro_bugDeteccion() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-nombre").param("nombre", "Ricardo"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.stackTrace").exists());
    }
}