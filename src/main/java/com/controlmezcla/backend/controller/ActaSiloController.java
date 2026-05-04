package com.controlmezcla.backend.controller;

import com.controlmezcla.backend.dto.ActaSiloRequest;
import com.controlmezcla.backend.model.ActaSilo;
import com.controlmezcla.backend.service.ActaSiloService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/acta-silo")
public class ActaSiloController {

    @Autowired
    private ActaSiloService acta_service;

    @PostMapping("/crear")
    public ResponseEntity<byte[]> crearActaSilo(
            @RequestPart("data") String data,
            @RequestPart(value = "imagenes", required = false)List<MultipartFile> imagenes)
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            ActaSiloRequest request = mapper.readValue(data, ActaSiloRequest.class);

            byte[] pdf = acta_service.crearActaSilo(request, imagenes);

            System.out.println("==== ACTA SILO CONTROLLER ======");
            System.out.println("CLIENTE: " + request.getCliente());
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=acta_silo.pdf")
                    .body(pdf);

        } catch (Exception e) {
            System.out.println("Error en el controlador: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    // Listar todas las actas
    @GetMapping("/listar")
    public ResponseEntity<List<ActaSilo>> listarActas() {
        try {
            return ResponseEntity.ok(acta_service.listarActas());
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    // Obtener acta por ID
    @GetMapping("/{id}")
    public ResponseEntity<ActaSilo> obtenerActa(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(acta_service.obtenerActaSilo(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    // Listar actas por técnico
    @GetMapping("/tecnico/{tecnicoId}")
    public ResponseEntity<List<ActaSilo>> listarPorTecnico(@PathVariable Long tecnicoId) {
        try {
            return ResponseEntity.ok(acta_service.listarPorTecnico(tecnicoId));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
