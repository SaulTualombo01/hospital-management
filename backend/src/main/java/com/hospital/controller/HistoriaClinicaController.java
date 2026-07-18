package com.hospital.controller;

import com.hospital.dto.HistoriaClinicaDTO;
import com.hospital.model.HistoriaClinica;
import com.hospital.service.HistoriaClinicaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/historias-clinicas")
@CrossOrigin(origins = "*")
public class HistoriaClinicaController {

    private final HistoriaClinicaService historiaService;

    public HistoriaClinicaController(HistoriaClinicaService historiaService) {
        this.historiaService = historiaService;
    }

    @GetMapping
    public ResponseEntity<List<HistoriaClinica>> listar() {
        return ResponseEntity.ok(historiaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoriaClinica> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(historiaService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<HistoriaClinica> crear(@Valid @RequestBody HistoriaClinicaDTO dto,
                                                 UriComponentsBuilder uriBuilder) {
        HistoriaClinica creada = historiaService.crear(dto);
        // FIX: se retorna 201 Created con header Location, en vez de 200 OK
        return ResponseEntity
                .created(uriBuilder.path("/api/historias-clinicas/{id}").buildAndExpand(creada.getId()).toUri())
                .body(creada);
    }

    @GetMapping("/paciente/{pacienteId}")
    public ResponseEntity<List<HistoriaClinica>> listarPorPaciente(@PathVariable Long pacienteId) {
        return ResponseEntity.ok(historiaService.listarPorPaciente(pacienteId));
    }

    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<List<HistoriaClinica>> listarPorDoctor(@PathVariable Long doctorId) {
        return ResponseEntity.ok(historiaService.listarPorDoctor(doctorId));
    }
}