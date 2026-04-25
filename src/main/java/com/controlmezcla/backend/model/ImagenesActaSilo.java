package com.controlmezcla.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import lombok.Data;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "imagenes_acta_silo")
public class ImagenesActaSilo {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruta_imagen;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime created_at;

    @ManyToOne
    @JoinColumn(name = "fk_acta_silo_id", nullable = false)
    private ActaSilo actaSilo;
}
