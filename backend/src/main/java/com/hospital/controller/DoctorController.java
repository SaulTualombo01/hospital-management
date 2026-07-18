package com.hospital.controller;

import com.hospital.dto.DoctorDTO;
import com.hospital.model.Doctor;
import com.hospital.service.DoctorService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

@RestController
@RequestMapping("/api/doctores")
@CrossOrigin(origins = "*")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public ResponseEntity<List<Doctor>> listar() {
        return ResponseEntity.ok(doctorService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Doctor> buscar(@PathVariable Long id) {
        return ResponseEntity.ok(doctorService.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Doctor> crear(@Valid @RequestBody DoctorDTO dto,
                                        UriComponentsBuilder uriBuilder) {
        Doctor creado = doctorService.crear(dto);
        // FIX: se retorna 201 Created con header Location, en vez de 200 OK
        return ResponseEntity
                .created(uriBuilder.path("/api/doctores/{id}").buildAndExpand(creado.getId()).toUri())
                .body(creado);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Doctor> actualizar(@PathVariable Long id, @Valid @RequestBody DoctorDTO dto) {
        return ResponseEntity.ok(doctorService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        doctorService.eliminar(id);
        // FIX: se retorna 204 No Content en vez de 200 OK
        return ResponseEntity.noContent().build();
    }

    // FIX: se usa el metodo seguro del servicio (parametrizado con Spring Data JPA),
    // el metodo original con concatenacion de strings fue eliminado del servicio
    @GetMapping("/buscar-especialidad")
    public ResponseEntity<List<Doctor>> buscarPorEspecialidad(@RequestParam String q) {
        return ResponseEntity.ok(doctorService.buscarPorEspecialidad(q));
    }

    @GetMapping("/buscar-nombre")
    public ResponseEntity<List<Doctor>> buscarPorNombreCompleto(@RequestParam String nombre,
                                                                @RequestParam String apellido) {
        return ResponseEntity.ok(doctorService.buscarPorNombreCompleto(nombre, apellido));
    }
}