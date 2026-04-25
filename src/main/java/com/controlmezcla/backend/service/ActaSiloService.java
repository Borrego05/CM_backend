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

    @Value("${app.storage.base}")
    private String storage_base;

    @Transactional
    public ActaSilo crearActaSilo(ActaSiloRequest request, List<MultipartFile> imagenes)
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
        acta.setFecha(LocalDate.parse(request.getFecha()));

        acta = acta_repository.save(acta);

        acta_repository.actualizarCodigo(
                acta.getId(),
                "AS-" + String.format("%03d", acta.getId())
        );

        acta.setCodigo_acta("AS-" + String.format("%03d", acta.getId()));

        String carpeta = storage_base + acta.getId();
        File directorio = new File(carpeta);

        if (!directorio.exists())
        {
            directorio.mkdirs();
        }

        List<String> rutas_imagenes = new ArrayList<>();

        try
        {
            if(imagenes != null && !imagenes.isEmpty())
            {
                int i = 1;
                for(MultipartFile imagen : imagenes)
                {
                    String nombre = "img_" + i + ".jpg";
                    imagen.transferTo(new File(carpeta + "/" +nombre));

                    ImagenesActaSilo imagen_silo = new ImagenesActaSilo();
                    imagen_silo.setRuta_imagen(nombre);
                    imagen_silo.setActaSilo(acta);
                    imagenes_repository.save(imagen_silo);

                    rutas_imagenes.add(nombre);
                    i++;
                }
            }

            pdf_service.GenerarPdf(acta, rutas_imagenes, carpeta);

        } catch (Exception e)
        {
            e.printStackTrace();
            throw new RuntimeException("Error guardando archivos: " + e.getMessage(), e);
        }

        return acta;
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
