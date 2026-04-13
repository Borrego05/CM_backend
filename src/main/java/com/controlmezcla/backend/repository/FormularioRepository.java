package com.controlmezcla.backend.repository;

import com.controlmezcla.backend.model.Formulario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.text.Normalizer;
import java.util.List;

@Repository
public interface FormularioRepository extends JpaRepository<Formulario, Long> {

    List<Formulario> findByTecnico_id(Long tecnico_id);
}
