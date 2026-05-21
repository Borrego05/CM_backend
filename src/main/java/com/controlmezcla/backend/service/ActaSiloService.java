package com.controlmezcla.backend.service;

import com.controlmezcla.backend.dto.ActaSiloRequest;
import com.controlmezcla.backend.model.ActaSilo;
import com.controlmezcla.backend.model.ImagenesActaSilo;
import com.controlmezcla.backend.model.Usuario;
import com.controlmezcla.backend.repository.ActaSiloRepository;
import com.controlmezcla.backend.repository.ImagenesActaSiloRepository;
import com.controlmezcla.backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ActaSiloService {

    @Autowired
    private ActaSiloRepository acta_repository;

    @Autowired
    private UsuarioRepository usuario_repository;

    @Autowired
    private ImagenesActaSiloRepository imagenes_repository;

    @Autowired
    private ActaSiloPdfService pdf_service;

    // ── ALMACENAMIENTO EN DISCO (desactivado temporalmente para Railway) ──────
    // Para reactivar: descomentar las líneas marcadas con [STORAGE] en este archivo,
    // en ActaSiloPdfService.java y en application.properties (app.storage.base / app.storage.pdf)
    // @Value("${app.storage.base}")
    // private String storage_base;                                             // [STORAGE]
    // @Value("${app.storage.pdf}")
    // private String pdf_path;                                                 // [STORAGE]

    @Transactional
    public byte[] crearActaSilo(ActaSiloRequest request, List<MultipartFile> imagenes, MultipartFile firmaCliente)
    {
        Usuario tecnico = usuario_repository.findById(request.getTecnico_id())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        ActaSilo acta = new ActaSilo();
        acta.setTecnico(tecnico);
        acta.setContacto(request.getContacto());
        acta.setCliente(request.getCliente());
        acta.setCedula(request.getCedula());
        acta.setCiudad_cedula(request.getCiudad_cedula());
        acta.setCiudad(request.getCiudad());
        acta.setObra(request.getObra());
        acta.setNumero_silo(request.getNumero_silo());
        acta.setNumero_toneladas(request.getNumero_toneladas());
        acta.setDescripcion(request.getDescripcion());
        acta.setNombre_tecnico(request.getNombre_tecnico());
        acta.setCedula_tecnico(request.getCedula_tecnico());
        acta.setTipo_mantenimiento(request.getTipo_mantenimiento());
        acta.setClase_mantenimiento(request.getClase_mantenimiento());
        acta.setTelefono_tecnico(request.getTelefono_tecnico());
        acta.setNombre_recibe(request.getNombre_recibe());
        acta.setCedula_recibe(request.getCedula_recibe());
        acta.setFecha(LocalDate.now());

        acta = acta_repository.save(acta);

        acta_repository.actualizarCodigo(
                acta.getId(),
                "CE-" + String.format("%03d", acta.getId())
        );
        acta.setCodigo_acta("CE-" + String.format("%03d", acta.getId()));

        // ── BLOQUE DE ALMACENAMIENTO EN DISCO (desactivado para Railway) ──────
        // Para reactivar: descomentar este bloque completo y comentar la sección
        // "GENERACIÓN SIN ARCHIVOS" de abajo.
        //
        // String carpeta = storage_base + acta.getId();                        // [STORAGE]
        // File directorio = new File(carpeta);                                 // [STORAGE]
        // if (!directorio.exists()) { directorio.mkdirs(); }                  // [STORAGE]
        //
        // List<String> rutas_imagenes = new ArrayList<>();                     // [STORAGE]
        // try {                                                                 // [STORAGE]
        //     String nombreFirma = "firma_cliente.png";                        // [STORAGE]
        //     firmaCliente.transferTo(new File(carpeta + "/" + nombreFirma));  // [STORAGE]
        //     acta.setFirma_cliente(nombreFirma);                              // [STORAGE]
        //     acta_repository.save(acta);                                      // [STORAGE]
        //                                                                       // [STORAGE]
        //     if (imagenes != null && !imagenes.isEmpty()) {                   // [STORAGE]
        //         int i = 1;                                                   // [STORAGE]
        //         for (MultipartFile imagen : imagenes) {                      // [STORAGE]
        //             String nombre = "img_" + i + ".jpg";                    // [STORAGE]
        //             imagen.transferTo(new File(carpeta + "/" + nombre));     // [STORAGE]
        //             ImagenesActaSilo imagen_silo = new ImagenesActaSilo();   // [STORAGE]
        //             imagen_silo.setRuta_imagen(nombre);                      // [STORAGE]
        //             imagen_silo.setActaSilo(acta);                           // [STORAGE]
        //             imagenes_repository.save(imagen_silo);                   // [STORAGE]
        //             rutas_imagenes.add(nombre);                              // [STORAGE]
        //             i++;                                                     // [STORAGE]
        //         }                                                            // [STORAGE]
        //     }                                                                // [STORAGE]
        //     String nombre_pdf = pdf_service.GenerarPdf(acta, rutas_imagenes, carpeta); // [STORAGE]
        //     return Files.readAllBytes(Paths.get(pdf_path + nombre_pdf));     // [STORAGE]
        // } catch (Exception e) {                                              // [STORAGE]
        //     e.printStackTrace();                                             // [STORAGE]
        //     throw new RuntimeException("Error guardando archivos: " + e.getMessage(), e); // [STORAGE]
        // }                                                                     // [STORAGE]
        // ── FIN BLOQUE ALMACENAMIENTO ─────────────────────────────────────────

        // ── GENERACIÓN SIN ARCHIVOS (activo para Railway) ─────────────────────
        try {
            return pdf_service.GenerarPdf(acta, new ArrayList<>(), "");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
        // ── FIN GENERACIÓN SIN ARCHIVOS ───────────────────────────────────────
    }

    public List<ActaSilo> listarActas()
    {
        return acta_repository.findAll();
    }

    public ActaSilo obtenerActaSilo(Long id)
    {
        return acta_repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Acta no encontrado"));
    }

    public List<ActaSilo> listarPorTecnico(Long tecnico_id)
    {
        return acta_repository.findByTecnicoId(tecnico_id);
    }
}
