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
    @DisplayName("POST /api/doctores - datos validos - retorna 201")
    void crear_datosValidos_retorna201() throws Exception {
        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre", is("Jorge")));
    }

    @Test
    @DisplayName("POST /api/doctores - nombre nulo - retorna 400")
    void crear_nombreNulo_retorna400() throws Exception {
        dtoValido.setNombre(null);

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists());
    }

    @Test
    @DisplayName("POST /api/doctores - apellido nulo - retorna 400")
    void crear_apellidoNulo_retorna400() throws Exception {
        dtoValido.setApellido(null);

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.apellido").exists());
    }

    @Test
    @DisplayName("POST /api/doctores - email nulo - retorna 400")
    void crear_emailNulo_retorna400() throws Exception {
        dtoValido.setEmail(null);

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /api/doctores - email vacio - retorna 400")
    void crear_emailVacio_retorna400() throws Exception {
        dtoValido.setEmail("");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/doctores - email duplicado - retorna 409 o 400")
    void crear_emailDuplicado_retornaError() throws Exception {
        doctorRepository.save(
                new Doctor("Otro", "Doctor", "Cardiologia", "jorge.suarez@hospital.com", "0991230099", "C-999"));

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/doctores - telefono con formato invalido - retorna 400")
    void crear_telefonoInvalido_retorna400() throws Exception {
        dtoValido.setTelefono("abc123");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/doctores - nombre con numeros - retorna 400")
    void crear_nombreConNumeros_retorna400() throws Exception {
        dtoValido.setNombre("Jorge123");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }

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
    @DisplayName("POST /api/doctores - especialidad vacia - retorna 400")
    void crear_especialidadVacia_retorna400() throws Exception {
        dtoValido.setEspecialidad("");

        mockMvc.perform(post("/api/doctores")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.especialidad").exists());
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

    // ---------- GET /api/doctores/ ----------

    @Test
    @DisplayName("GET /api/doctores - lista con datos - retorna 200 con doctores")
    void listar_conDatos_retornaLista() throws Exception {
        doctorRepository.save(
                new Doctor("Ana", "Torres", "Cardiologia", "ana.torres@hospital.com", "0991230001", "C-101"));

        mockMvc.perform(get("/api/doctores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/doctores - sin datos - retorna 200 con lista vacia")
    void listar_sinDatos_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/doctores"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
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
    @DisplayName("GET /api/doctores/{id} - id inexistente - retorna 404")
    void buscar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/doctores/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    @Test
    @DisplayName("GET /api/doctores/{id} - id no numerico - retorna 400")
    void buscar_idNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get("/api/doctores/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/doctores/{id} - id negativo - retorna 404")
    void buscar_idNegativo_retorna404() throws Exception {
        mockMvc.perform(get("/api/doctores/{id}", -1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }

    // ---------- PUT /api/doctores/{id} ----------

    @Test
    @DisplayName("PUT /api/doctores/{id} - id inexistente - retorna 404")
    void actualizar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(put("/api/doctores/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/doctores/{id} - nombre vacio en actualizacion - retorna 400")
    void actualizar_nombreVacio_retorna400() throws Exception {
        Doctor guardado = doctorRepository.save(
                new Doctor("Pedro", "Salas", "Nefrologia", "pedro.salas@hospital.com", "0991230011", "C-808"));

        DoctorDTO actualizacion = new DoctorDTO();
        actualizacion.setNombre("");
        actualizacion.setApellido("Salas");
        actualizacion.setEspecialidad("Nefrologia");
        actualizacion.setEmail("pedro.salas@hospital.com");
        actualizacion.setTelefono("0991230011");
        actualizacion.setConsultorio("C-808");

        mockMvc.perform(put("/api/doctores/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/doctores/{id} - email invalido en actualizacion - retorna 400")
    void actualizar_emailInvalido_retorna400() throws Exception {
        Doctor guardado = doctorRepository.save(
                new Doctor("Luis", "Rios", "Cardiologia", "luis.rios@hospital.com", "0991230022", "C-909"));

        DoctorDTO actualizacion = new DoctorDTO();
        actualizacion.setNombre("Luis");
        actualizacion.setApellido("Rios");
        actualizacion.setEspecialidad("Cardiologia");
        actualizacion.setEmail("correo-invalido");
        actualizacion.setTelefono("0991230022");
        actualizacion.setConsultorio("C-909");

        mockMvc.perform(put("/api/doctores/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isBadRequest());
    }

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
    @DisplayName("DELETE /api/doctores/{id} - id inexistente - retorna 404")
    void eliminar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(delete("/api/doctores/{id}", 999999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/doctores/{id} - elimina correctamente - retorna 204")
    void eliminar_idExistente_retorna204() throws Exception {
        Doctor guardado = doctorRepository.save(
                new Doctor("Sara", "Nunez", "Dermatologia", "sara.nunez@hospital.com", "0991110000", "C-404"));

        mockMvc.perform(delete("/api/doctores/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                doctorRepository.findById(guardado.getId()).isPresent());
    }

    // ---------- GET /api/doctores/buscar-especialidad (SQL Injection corregido) ----------

    @Test
    @DisplayName("GET /api/doctores/buscar-especialidad?q= - busqueda normal - retorna coincidencias")
    void buscarPorEspecialidad_valorNormal_retornaCoincidencias() throws Exception {
        doctorRepository.save(new Doctor("Diego", "Leon", "Neurologia", "diego.leon@hospital.com", "0991112222", "C-505"));

        mockMvc.perform(get("/api/doctores/buscar-especialidad").param("q", "Neuro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-especialidad?q= - payload de SQL Injection - ya no explota (fix aplicado)")
    void buscarPorEspecialidad_payloadSqlInjection_noExplota() throws Exception {
        doctorRepository.save(new Doctor("Nora", "Cabrera", "Oncologia", "nora.cabrera@hospital.com", "0991113333", "C-606"));
        doctorRepository.save(new Doctor("Marta", "Ibarra", "Ginecologia", "marta.ibarra@hospital.com", "0991114455", "C-707"));

        String payload = "%' OR '1'='1' -- ";

        mockMvc.perform(get("/api/doctores/buscar-especialidad").param("q", payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0))); // ahora se trata como texto literal, sin coincidencias
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-especialidad?q= - valor vacio - retorna lista vacia")
    void buscarPorEspecialidad_valorVacio_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-especialidad").param("q", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-especialidad?q= - sin coincidencias - retorna lista vacia")
    void buscarPorEspecialidad_sinCoincidencias_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-especialidad").param("q", "EspecialidadInventada"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-especialidad - parametro q faltante - retorna 400")
    void buscarPorEspecialidad_parametroFaltante_retorna400() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-especialidad"))
                .andExpect(status().isBadRequest());
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
    @DisplayName("GET /api/doctores/buscar-nombre - sin coincidencia - retorna lista vacia")
    void buscarPorNombreCompleto_sinCoincidencia_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-nombre")
                        .param("nombre", "Inexistente")
                        .param("apellido", "Nadie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-nombre - parametros vacios - retorna 400")
    void buscarPorNombreCompleto_parametrosVacios_retorna400() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-nombre")
                        .param("nombre", "")
                        .param("apellido", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/doctores/buscar-nombre - falta parametro obligatorio - retorna 400")
    void buscarPorNombreCompleto_faltaParametro_retorna400() throws Exception {
        mockMvc.perform(get("/api/doctores/buscar-nombre").param("nombre", "Ricardo"))
                .andExpect(status().isBadRequest());
    }
}