package com.controlmezcla.backend.service;

import com.controlmezcla.backend.dto.FormularioRequest;
import com.controlmezcla.backend.model.Formulario;
import com.controlmezcla.backend.model.Usuario;
import com.controlmezcla.backend.repository.FormularioRepository;
import com.controlmezcla.backend.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FormularioService {

    @Autowired
    private  FormularioRepository formulario_repository;

    @Autowired
    private  UsuarioRepository usuario_repository;

    public Formulario crearFormulario(FormularioRequest request)
    {
        Usuario tecnico = usuario_repository.findByUsuario(String.valueOf(request.getFk_tecnico_id()))
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
        formulario.setTecnico(tecnico);

        return formulario_repository.save(formulario);
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
