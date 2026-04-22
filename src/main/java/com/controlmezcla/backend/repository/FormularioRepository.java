package com.controlmezcla.backend.repository;

import com.controlmezcla.backend.model.Formulario;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.text.Normalizer;
import java.util.List;

@Repository
public interface FormularioRepository extends JpaRepository<Formulario, Long> {

    List<Formulario> findByTecnico_id(Long tecnico_id);

    @Modifying
    @Transactional
    @Query("UPDATE Formulario f SET f.codigo_informe = :codigo WHERE f.id = :id")
    void actualizarCodigo(@Param("id") Long id, @Param("codigo") String codigo);
}
