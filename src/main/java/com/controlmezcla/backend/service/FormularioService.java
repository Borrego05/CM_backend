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
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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

    @Value("${app.storage.base}")
    private String storageBase;

    @Transactional
    public Formulario crearFormulario(
            FormularioRequest request,
            List<MultipartFile> imagenes,
            MultipartFile firmaCliente,
            MultipartFile firmaTecnico
    )
    {
        Usuario tecnico = usuario_repository.findById(request.getFk_tecnico_id())
                .orElseThrow(()-> new RuntimeException("Tecnico no encontrado"));

        Formulario formulario = new Formulario();

        formulario.setCliente(request.getCliente());
        formulario.setDireccion(request.getDireccion());
        formulario.setObra(request.getObra());
        formulario.setTelefono(request.getTelefono());
        formulario.setFecha(LocalDate.parse(request.getFecha()));
        formulario.setDescripcion(request.getDescripcion());
        formulario.setMateriales_utilizados(request.getMateriales_utilizados());
        formulario.setClases_mantenimiento(request.getClases_mantenimiento());
        formulario.setTipo_mantenimiento(request.getTipo_mantenimiento());
        formulario.setContacto(request.getContacto());
        formulario.setTecnico(tecnico);

        formulario = formulario_repository .save(formulario);

        //Generacion del codigo con ID
        formulario_repository.actualizarCodigo(
                formulario.getId(),
                "CM-" + String.format("%03d", formulario.getId())
        );

        formulario.setCodigo_informe("CM-" + String.format("%03d", formulario.getId()));

        String carpeta = storageBase + formulario.getId();
        File directorio = new File(carpeta);
        if (!directorio.exists())
        {
            directorio.mkdirs();
        }

        List<String> rutasImagenes = new ArrayList<>();

        try
        {
            String nombreFirmaCliente = "firma_cliente.png";
            String rutaFirmaCliente = carpeta + "/" + nombreFirmaCliente;
            firmaCliente.transferTo(new File(rutaFirmaCliente));
            formulario.setFirma_cliente(nombreFirmaCliente);

            String nombreFirmaTecnico = "firma_tecnico.png";
            String rutaFirmaTecnico = carpeta + "/" + nombreFirmaTecnico;
            firmaTecnico.transferTo(new File(rutaFirmaTecnico));
            formulario.setFirma_tecnico(nombreFirmaTecnico);

            formulario_repository.save(formulario);

            int i = 1;
            for (MultipartFile img : imagenes)
            {
                String nombreImagen = "img_" + i + ".jpg";
                String rutaImagen = carpeta + "/" + nombreImagen;

                img.transferTo(new File(rutaImagen));

                Imagenes imagen = new Imagenes();
                imagen.setRuta_imagen(nombreImagen);
                imagen.setFormulario_id(formulario);
                imagen.setCreated_at(LocalDate.now());

                imagenes_repository.save(imagen);

                rutasImagenes.add(nombreImagen);
                i++;
            }

            pdf_service.generarPDF(formulario, rutasImagenes, carpeta);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error guardando archivos: " + e.getMessage(), e);
        }

        return formulario;
    }

//    public Formulario crearFormulario(FormularioRequest request)
//    {
//        Usuario tecnico = usuario_repository.findById(request.getFk_tecnico_id())
//                .orElseThrow(()-> new RuntimeException("Tecnico no encontrado"));
//
//        Formulario formulario = new Formulario();
//
//        formulario.setCliente(request.getCliente());
//        formulario.setDireccion(request.getDireccion());
//        formulario.setObra(request.getObra());
//        formulario.setTelefono(request.getTelefono());
//        formulario.setFecha(LocalDate.parse(request.getFecha()));
//        formulario.setDescripcion(request.getDescripcion());
//        formulario.setMateriales_utilizados(request.getMateriales_utilizados());
//        formulario.setClases_mantenimiento(request.getClases_mantenimiento());
//        formulario.setTipo_mantenimiento(request.getTipo_mantenimiento());
//        formulario.setTecnico(tecnico);
//
//        return formulario_repository.save(formulario);
//    }

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
