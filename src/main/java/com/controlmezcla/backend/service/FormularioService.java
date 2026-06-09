package com.controlmezcla.backend.service;

import com.controlmezcla.backend.dto.FormularioRequest;
import com.controlmezcla.backend.model.Formulario;
import com.controlmezcla.backend.model.Imagenes;
import com.controlmezcla.backend.model.Usuario;
import com.controlmezcla.backend.repository.FormularioRepository;
import com.controlmezcla.backend.repository.ImagenesRepository;
import com.controlmezcla.backend.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cglib.core.Local;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class FormularioService {

    @Autowired
    private  FormularioRepository formulario_repository;

    @Autowired
    private  UsuarioRepository usuario_repository;

    @Autowired
    private ImagenesRepository imagenes_repository;

    @Autowired
    private PdfService pdf_service;

    // ── ALMACENAMIENTO EN DISCO (desactivado temporalmente para Railway) ──────
    // Para reactivar: descomentar las líneas marcadas con [STORAGE] en este archivo,
    // en PdfService.java y en application.properties (app.storage.base / app.storage.pdf)
    // @Value("${app.storage.base}")
    // private String storageBase;                                              // [STORAGE]
    // @Value("${app.storage.pdf}")
    // private String pdfPath;                                                  // [STORAGE]

    @Transactional
    public byte[] crearFormulario(
            FormularioRequest request,
            List<MultipartFile> imagenes,
            MultipartFile firmaCliente
    )
    {
        Usuario tecnico = usuario_repository.findById(request.getFk_tecnico_id())
                .orElseThrow(()-> new RuntimeException("Tecnico no encontrado"));

        Formulario formulario = new Formulario();

        formulario.setCliente(request.getCliente());
        formulario.setDireccion(request.getDireccion());
        formulario.setObra(request.getObra());
        formulario.setTelefono(request.getTelefono());
        formulario.setFecha(LocalDate.now());
        formulario.setDescripcion(request.getDescripcion());
        formulario.setMateriales_utilizados(request.getMateriales_utilizados());
        formulario.setClases_mantenimiento(request.getClases_mantenimiento());
        formulario.setTipo_mantenimiento(request.getTipo_mantenimiento());
        formulario.setContacto(request.getContacto());
        formulario.setNombre_tecnico(request.getNombre_tecnico());
        formulario.setTelefono_tecnico(request.getTelefono_tecnico());
        formulario.setNombre_recibe(request.getNombre_recibe());
        formulario.setCedula_recibe(request.getCedula_recibe());
        formulario.setCalificacion(request.getCalificacion());
        formulario.setComentario_calificacion(request.getComentario_calificacion());
        formulario.setTecnico(tecnico);

        formulario = formulario_repository.save(formulario);

        // Generación del código con ID
        formulario_repository.actualizarCodigo(
                formulario.getId(),
                "CM-" + String.format("%03d", formulario.getId())
        );
        formulario.setCodigo_informe("CM-" + String.format("%03d", formulario.getId()));

        // ── BLOQUE DE ALMACENAMIENTO EN DISCO (desactivado para Railway) ──────
        // Para reactivar: descomentar este bloque completo y comentar la sección
        // "GENERACIÓN SIN ARCHIVOS" de abajo.
        //
        // String carpeta = storageBase + formulario.getId();                   // [STORAGE]
        // File directorio = new File(carpeta);                                 // [STORAGE]
        // if (!directorio.exists()) { directorio.mkdirs(); }                  // [STORAGE]
        //
        // List<String> rutasImagenes = new ArrayList<>();                      // [STORAGE]
        // try {                                                                 // [STORAGE]
        //     String nombreFirmaCliente = "firma_cliente.png";                 // [STORAGE]
        //     String rutaFirmaCliente = carpeta + "/" + nombreFirmaCliente;    // [STORAGE]
        //     firmaCliente.transferTo(new File(rutaFirmaCliente));             // [STORAGE]
        //     formulario.setFirma_cliente(nombreFirmaCliente);                 // [STORAGE]
        //     formulario_repository.save(formulario);                          // [STORAGE]
        //                                                                       // [STORAGE]
        //     int i = 1;                                                       // [STORAGE]
        //     for (MultipartFile img : imagenes) {                             // [STORAGE]
        //         String nombreImagen = "img_" + i + ".jpg";                  // [STORAGE]
        //         String rutaImagen = carpeta + "/" + nombreImagen;            // [STORAGE]
        //         img.transferTo(new File(rutaImagen));                        // [STORAGE]
        //         Imagenes imagen = new Imagenes();                            // [STORAGE]
        //         imagen.setRuta_imagen(nombreImagen);                         // [STORAGE]
        //         imagen.setFormulario_id(formulario);                         // [STORAGE]
        //         imagen.setCreated_at(LocalDate.now());                       // [STORAGE]
        //         imagenes_repository.save(imagen);                            // [STORAGE]
        //         rutasImagenes.add(nombreImagen);                             // [STORAGE]
        //         i++;                                                         // [STORAGE]
        //     }                                                                // [STORAGE]
        //     String nombre_pdf = pdf_service.generarPDF(formulario, rutasImagenes, carpeta); // [STORAGE]
        //     return Files.readAllBytes(Paths.get(pdfPath + nombre_pdf));      // [STORAGE]
        // } catch (Exception e) {                                              // [STORAGE]
        //     e.printStackTrace();                                             // [STORAGE]
        //     throw new RuntimeException("Error guardando archivos: " + e.getMessage(), e); // [STORAGE]
        // }                                                                     // [STORAGE]
        // ── FIN BLOQUE ALMACENAMIENTO ─────────────────────────────────────────

        // ── GENERACIÓN EN MEMORIA (activo para Railway) ───────────────────────
        // Convierte los MultipartFile a byte[] para pasarlos al PDF sin tocar disco.
        try {
            List<byte[]> imagenes_bytes = new ArrayList<>();
            if (imagenes != null) {
                for (MultipartFile img : imagenes) {
                    imagenes_bytes.add(img.getBytes());
                }
            }
            byte[] firma_bytes = (firmaCliente != null && !firmaCliente.isEmpty())
                    ? firmaCliente.getBytes()
                    : null;
            return pdf_service.generarPDF(formulario, imagenes_bytes, firma_bytes);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Error generando PDF: " + e.getMessage(), e);
        }
        // ── FIN GENERACIÓN EN MEMORIA ─────────────────────────────────────────
    }

    public List<Formulario> listarFormularios()
    {
        return formulario_repository.findAll();
    }

    public Formulario obtenerFormulario(Long id)
    {
        return formulario_repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Formulario no encontrado"));
    }

    public List<Formulario> listarPorTecnico(Long tecnico_id)
    {
        return formulario_repository.findByTecnico_id(tecnico_id);
    }
}
