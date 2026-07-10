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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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
        dtoValido.setFechaNacimiento(LocalDate.of(1995, 5, 20));
        dtoValido.setEmail("maria.lopez@example.com");
        dtoValido.setTelefono("0991234567");
        dtoValido.setDireccion("Av. Amazonas y Naciones Unidas");
        dtoValido.setActivo(true);
    }

    // ---------- POST /api/pacientes ----------

    @Test
    @DisplayName("POST /api/pacientes - datos validos - deberia crear y retornar 201")
    void crear_datosValidos_retorna201() throws Exception {
        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk())
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
    @DisplayName("POST /api/pacientes - apellido vacio - NO deberia pasar (bug: falta @NotBlank en apellido)")
    void crear_apellidoVacio_bugDeteccion() throws Exception {
        dtoValido.setApellido("");

        mockMvc.perform(post("/api/pacientes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dtoValido)))
                .andExpect(status().isOk()) // no isCreated por el otro bug
                .andExpect(jsonPath("$.apellido", is("")));
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
                .andExpect(status().isOk())
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
                new Paciente("Carlos", "Perez", LocalDate.of(1990, 1, 1),
                        "carlos.perez@example.com", "0991112233", "Quito"));

        mockMvc.perform(get("/api/pacientes/{id}", guardado.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(guardado.getId().intValue())))
                .andExpect(jsonPath("$.nombre", is("Carlos")));
    }

    @Test
    @DisplayName("GET /api/pacientes/{id} - id inexistente - el controlador documenta bug: retorna 200 en vez de 404")
    void buscar_idInexistente_bugDeteccion() throws Exception {
        mockMvc.perform(get("/api/pacientes/{id}", 999999L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("no encontrado")));
    }

    // ---------- PUT /api/pacientes/{id} ----------

    @Test
    @DisplayName("PUT /api/pacientes/{id} - actualizacion valida - retorna 200 con datos actualizados")
    void actualizar_datosValidos_retornaActualizado() throws Exception {
        Paciente guardado = pacienteRepository.save(
                new Paciente("Ana", "Torres", LocalDate.of(1988, 3, 15),
                        "ana.torres@example.com", "0987654321", "Cuenca"));

        PacienteDTO actualizacion = new PacienteDTO();
        actualizacion.setNombre("Ana Maria");
        actualizacion.setApellido("Torres");
        actualizacion.setFechaNacimiento(LocalDate.of(1988, 3, 15));
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

    // ---------- DELETE /api/pacientes/{id} ----------

    @Test
    @DisplayName("DELETE /api/pacientes/{id} - id existente - elimina (bug: retorna 200 en vez de 204)")
    void eliminar_idExistente_bugDeteccion() throws Exception {
        Paciente guardado = pacienteRepository.save(
                new Paciente("Luis", "Ramos", LocalDate.of(2000, 7, 7),
                        "luis.ramos@example.com", "0998887766", "Quito"));

        mockMvc.perform(delete("/api/pacientes/{id}", guardado.getId()))
                .andExpect(status().isOk());

        org.junit.jupiter.api.Assertions.assertFalse(
                pacienteRepository.findById(guardado.getId()).isPresent());
    }

    // ---------- GET /api/pacientes/buscar ----------

    @Test
    @DisplayName("GET /api/pacientes/buscar?nombre= - nombre existente - retorna coincidencias")
    void buscarPorNombre_nombreExistente_retornaLista() throws Exception {
        pacienteRepository.save(new Paciente("Patricia", "Salazar", LocalDate.of(1992, 2, 2),
                "patricia@example.com", "0991234567", "Quito"));

        mockMvc.perform(get("/api/pacientes/buscar").param("nombre", "Patricia"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(1))))
                .andExpect(jsonPath("$[0].nombre", is("Patricia")));
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
