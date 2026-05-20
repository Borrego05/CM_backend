package com.controlmezcla.backend.repository;

import com.controlmezcla.backend.model.ImagenesActaSilo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImagenesActaSiloRepository extends JpaRepository<ImagenesActaSilo, Long> {

    List<ImagenesActaSilo> findByActaSiloId(Long actaSiloId);
}
