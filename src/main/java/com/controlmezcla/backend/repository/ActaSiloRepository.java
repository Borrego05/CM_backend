package com.controlmezcla.backend.repository;

import com.controlmezcla.backend.model.ActaSilo;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActaSiloRepository extends JpaRepository<ActaSilo, Long> {

    //Buscar Actas por tecnico
    List<ActaSilo> findByTecnicoId(Long tecnico_id);

    //Actualizar el codigo del acta
    @Modifying
    @Transactional
    @Query("UPDATE ActaSilo a SET a.codigo_acta = :codigo WHERE a.id = :id")
    void actualizarCodigo(@Param("id") Long id, @Param("codigo") String codigo);
}
