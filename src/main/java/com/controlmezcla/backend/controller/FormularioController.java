package com.controlmezcla.backend.controller;

import com.controlmezcla.backend.dto.FormularioRequest;
import com.controlmezcla.backend.model.Formulario;
import com.controlmezcla.backend.service.FormularioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/formulario")
public class FormularioController {

    @Autowired
    private FormularioService formulario_service;

    @PostMapping(value = "/crear")
    public ResponseEntity<byte []> crearFormulario(
            @RequestPart("data") String data,
            @RequestPart(value = "imagenes", required = false) List<MultipartFile> imagenes,
            @RequestPart("firmaCliente") MultipartFile firmaCliente,
            @RequestPart("firmaTecnico") MultipartFile firmaTecnico
            )
    {
        try
        {
            ObjectMapper mapper = new ObjectMapper();
            FormularioRequest request = mapper.readValue(data, FormularioRequest.class);

            byte[] pdf = formulario_service.crearFormulario(request, imagenes, firmaCliente, firmaTecnico);

            System.out.println("===== FOMULARIO CONTROLLER =====");
            System.out.println("Cliente: " + request.getCliente());

            //formulario_service.crearFormulario(request, imagenes, firmaCliente, firmaTecnico);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/pdf")
                    .header("Content-Disposition", "attachment; filename=informe.pdf")
                    .body(pdf);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/listar")
    public ResponseEntity<List<Formulario>> listarFormularios()
    {
        try
        {
            List<Formulario> formularios = formulario_service.listarFormularios();
            return ResponseEntity.ok(formularios);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Formulario> obtenerFormulario(@PathVariable Long id)
    {
        try
        {
            Formulario formulario = formulario_service.obtenerFormulario(id);
            return ResponseEntity.ok(formulario);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/tecnico/{tecnico_id}")
    public ResponseEntity<List<Formulario>> listarPorTecnico(@PathVariable Long tecnico_id)
    {
        try
        {
            List<Formulario> formularios = formulario_service.listarPorTecnico(tecnico_id);
            return ResponseEntity.ok(formularios);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.badRequest().build();
        }
    }

}
