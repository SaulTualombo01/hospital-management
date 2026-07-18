package com.hospital.controller;

import com.hospital.dto.PacienteDTO;
import com.hospital.model.Paciente;
import com.hospital.service.PacienteService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/pacientes")
@CrossOrigin(origins = "*")  // BUG INTENCIONAL: CORS demasiado permisivo (OWASP)
public class PacienteController {

    private final PacienteService pacienteService;

    public PacienteController(PacienteService pacienteService) {
        this.pacienteService = pacienteService;
    }

    @GetMapping
    public ResponseEntity<List<Paciente>> listar() {
        return ResponseEntity.ok(pacienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Paciente> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(pacienteService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Paciente> crear(@Valid @RequestBody PacienteDTO dto, UriComponentsBuilder uriBuilder) {
        Paciente creado = pacienteService.crear(dto);
        // FIX: ahora retorna 201 Created con header Location (antes retornaba 200 OK)
        var location = uriBuilder.path("/api/pacientes/{id}").buildAndExpand(creado.getId()).toUri();
        return ResponseEntity.created(location).body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Paciente> actualizar(@PathVariable Long id, @Valid @RequestBody PacienteDTO dto) {
        return ResponseEntity.ok(pacienteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        pacienteService.eliminar(id);
        // FIX: ahora retorna 204 No Content (antes retornaba 200 OK)
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/buscar")
    public ResponseEntity<List<Paciente>> buscarPorNombre(@RequestParam String nombre) {
        return ResponseEntity.ok(pacienteService.buscarPorNombre(nombre));
    }

    @GetMapping("/estadisticas/edad-promedio")
    public ResponseEntity<Double> edadPromedio() {
        return ResponseEntity.ok(pacienteService.calcularEdadPromedio());
    }
}