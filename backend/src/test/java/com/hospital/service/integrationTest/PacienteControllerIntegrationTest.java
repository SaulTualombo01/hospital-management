package com.hospital.service.integrationTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.dto.PacienteDTO;
import com.hospital.model.Paciente;
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
import java.time.Month;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class PacienteControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PacienteRepository pacienteRepository;

    private PacienteDTO dtoValido;

    @BeforeEach
    void setUp() {
        pacienteRepository.deleteAll();

        dtoValido = new PacienteDTO();
        dtoValido.setNombre("Maria");
        dtoValido.setApellido("Lopez");
        dtoValido.setFechaNacimiento(LocalDate.of(1995, Month.MAY, 20));
        dtoValido.setEmail("maria.lopez@example.com");
        dtoValido.setTelefono("0991234567");
        dtoValido.setDireccion("Av. Amazonas y Naciones Unidas");
        dtoValido.setActivo(true);
    }

    // ---------- POST /api/pacientes ----------

    @Test
    @DisplayName("POST /api/pacientes - nombre nulo - retorna 400")
    void crear_nombreNulo_retorna400() throws Exception {
        dtoValido.setNombre(null);

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - email nulo - retorna 400")
    void crear_emailNulo_retorna400() throws Exception {
        dtoValido.setEmail(null);

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - email duplicado - retorna error 4xx")
    void crear_emailDuplicado_retornaError() throws Exception {
        pacienteRepository.save(new Paciente("Otro", "Paciente", LocalDate.of(1980, Month.JANUARY, 1),
                "maria.lopez@example.com", "0990001122", "Quito"));

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().is4xxClientError());
    }

    @Test
    @DisplayName("POST /api/pacientes - telefono con letras - retorna 400")
    void crear_telefonoConLetras_retorna400() throws Exception {
        dtoValido.setTelefono("09876abcde");

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.telefono").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - fecha de nacimiento de hoy (valor limite invalido por @Past) - retorna 400")
    void crear_fechaNacimientoHoy_retorna400() throws Exception {
        dtoValido.setFechaNacimiento(LocalDate.now());

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/pacientes - fecha de nacimiento nula - retorna 400")
    void crear_fechaNacimientoNula_retorna400() throws Exception {
        dtoValido.setFechaNacimiento(null);

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fechaNacimiento").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - datos validos - retorna 201")
    void crear_datosValidos_retorna201() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre", is("Maria")))
                .andExpect(jsonPath("$.apellido", is("Lopez")));
    }

    @Test
    @DisplayName("POST /api/pacientes - nombre vacio (particion invalida) - retorna 400")
    void crear_nombreVacio_retorna400() throws Exception {
        dtoValido.setNombre("");

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nombre").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - apellido vacio - retorna 400")
    void crear_apellidoVacio_retorna400() throws Exception {
        dtoValido.setApellido("");

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.apellido").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - email con formato invalido - retorna 400")
    void crear_emailInvalido_retorna400() throws Exception {
        dtoValido.setEmail("no-es-un-email");

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - telefono con longitud invalida (limite: 10 digitos) - retorna 400")
    void crear_telefonoInvalido_retorna400() throws Exception {
        dtoValido.setTelefono("12345");

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.telefono").exists());
    }

    @Test
    @DisplayName("POST /api/pacientes - telefono con exactamente 10 digitos (valor limite valido) - se acepta")
    void crear_telefonoConDiezDigitos_esValido() throws Exception {
        dtoValido.setTelefono("0987654321");

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.telefono", is("0987654321")));
    }

    @Test
    @DisplayName("POST /api/pacientes - fecha de nacimiento futura (invalida por @Past) - retorna 400")
    void crear_fechaNacimientoFutura_retorna400() throws Exception {
        dtoValido.setFechaNacimiento(LocalDate.now().plusDays(1));

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.fechaNacimiento").exists());
    }

    // ---------- GET /api/pacientes/{id} ----------

    @Test
    @DisplayName("GET /api/pacientes/{id} - id existente - retorna 200 y el paciente")
    void buscar_idExistente_retornaPaciente() throws Exception {
        Paciente guardado = pacienteRepository.save(
                new Paciente("Carlos", "Perez", LocalDate.of(1990, Month.JANUARY, 1),
                        "carlos.perez@example.com", "0991112233", "Quito"));

        mockMvc.perform(get("/api/pacientes/{id}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(guardado.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Carlos")));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} - id inexistente - retorna 404")
    void buscar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(get("/api/pacientes/{id}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("no encontrado")));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} - id no numerico - retorna 400")
    void buscar_idNoNumerico_retorna400() throws Exception {
        mockMvc.perform(get("/api/pacientes/{id}", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} - id negativo - retorna 404")
    void buscar_idNegativo_retorna404() throws Exception {
        mockMvc.perform(get("/api/pacientes/{id}", -1L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)));
    }
    // ---------- PUT /api/pacientes/{id} ----------

    @Test
    @DisplayName("PUT /api/pacientes/{id} - actualizacion valida - retorna 200 con datos actualizados")
    void actualizar_datosValidos_retornaActualizado() throws Exception {
        Paciente guardado = pacienteRepository.save(
                new Paciente("Ana", "Torres", LocalDate.of(1988, Month.MARCH, 15),
                        "ana.torres@example.com", "0987654321", "Cuenca"));

        PacienteDTO actualizacion = new PacienteDTO();
        actualizacion.setNombre("Ana Maria");
        actualizacion.setApellido("Torres");
        actualizacion.setFechaNacimiento(LocalDate.of(1988, Month.MARCH, 15));
        actualizacion.setEmail("ana.maria@example.com");
        actualizacion.setTelefono("0987654322");
        actualizacion.setDireccion("Cuenca centro");
        actualizacion.setActivo(true);

        mockMvc.perform(put("/api/pacientes/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre", is("Ana Maria")))
                .andExpect(jsonPath("$.email", is("ana.maria@example.com")));
    }

    @Test
    @DisplayName("PUT /api/pacientes/{id} - id inexistente - retorna 404")
    void actualizar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(put("/api/pacientes/{id}", 999999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/pacientes/{id} - email invalido en actualizacion - retorna 400")
    void actualizar_emailInvalido_retorna400() throws Exception {
        Paciente guardado = pacienteRepository.save(
                new Paciente("Jorge", "Vera", LocalDate.of(1985, Month.APRIL, 10),
                        "jorge.vera@example.com", "0991230000", "Quito"));

        PacienteDTO actualizacion = new PacienteDTO();
        actualizacion.setNombre("Jorge");
        actualizacion.setApellido("Vera");
        actualizacion.setFechaNacimiento(LocalDate.of(1985, Month.APRIL, 10));
        actualizacion.setEmail("correo-invalido");
        actualizacion.setTelefono("0991230000");
        actualizacion.setDireccion("Quito");
        actualizacion.setActivo(true);

        mockMvc.perform(put("/api/pacientes/{id}", guardado.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(actualizacion)))
                .andExpect(status().isBadRequest());
    }

    // ---------- DELETE /api/pacientes/{id} ----------

    @Test
    @DisplayName("DELETE /api/pacientes/{id} - id existente - retorna 204")
    void eliminar_idExistente_retorna204() throws Exception {
        Paciente guardado = pacienteRepository.save(
                new Paciente("Luis", "Ramos", LocalDate.of(2000, Month.JULY, 7),
                        "luis.ramos@example.com", "0998887766", "Quito"));

        mockMvc.perform(delete("/api/pacientes/{id}", guardado.getId()))
                .andExpect(status().isNoContent());

        org.junit.jupiter.api.Assertions.assertFalse(
                pacienteRepository.findById(guardado.getId()).isPresent());
    }

    @Test
    @DisplayName("DELETE /api/pacientes/{id} - id inexistente - retorna 404")
    void eliminar_idInexistente_retorna404() throws Exception {
        mockMvc.perform(delete("/api/pacientes/{id}", 999999L))
                .andExpect(status().isNotFound());
    }
    // ---------- GET /api/pacientes/buscar ----------

    @Test
    @DisplayName("GET /api/pacientes/buscar?nombre= - nombre existente - retorna coincidencias")
    void buscarPorNombre_nombreExistente_retornaLista() throws Exception {
        pacienteRepository.save(new Paciente("Patricia", "Salazar", LocalDate.of(1992, Month.FEBRUARY, 2),
                "patricia@example.com", "0991234567", "Quito"));

        mockMvc.perform(get("/api/pacientes/buscar").param("nombre", "Patricia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nombre", is("Patricia")));
    }

    @Test
    @DisplayName("GET /api/pacientes - lista con datos - retorna 200 con pacientes")
    void listar_conDatos_retornaLista() throws Exception {
        pacienteRepository.save(new Paciente("Elena", "Rios", LocalDate.of(1995, Month.JUNE, 1),
                "elena.rios@example.com", "0991230011", "Quito"));

        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("GET /api/pacientes - sin datos - retorna 200 con lista vacia")
    void listar_sinDatos_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/pacientes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/pacientes/buscar - sin resultados - retorna lista vacia")
    void buscarPorNombre_sinCoincidencias_retornaListaVacia() throws Exception {
        mockMvc.perform(get("/api/pacientes/buscar").param("nombre", "NombreQueNoExiste123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }


    @Test
    @DisplayName("GET /api/pacientes/estadisticas/edad-promedio - con pacientes - calcula promedio")
    void edadPromedio_conPacientes_calculaCorrectamente() throws Exception {
        pacienteRepository.save(new Paciente("P1", "A", LocalDate.now().minusYears(20),
                "p1@example.com", "0991111111", "Quito"));
        pacienteRepository.save(new Paciente("P2", "B", LocalDate.now().minusYears(30),
                "p2@example.com", "0992222222", "Quito"));

        mockMvc.perform(get("/api/pacientes/estadisticas/edad-promedio"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", is(25.0)));
    }

    @Test
    @DisplayName("GET /api/pacientes/estadisticas/edad-promedio - sin pacientes - bug: division por cero (500 / NaN)")
    void edadPromedio_sinPacientes_bugDivisionPorCero() throws Exception {
        mockMvc.perform(get("/api/pacientes/estadisticas/edad-promedio"))
                .andExpect(status().isOk());
    }
}
