package com.controlmezcla.backend.model;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Data
@Table(name = "imagenes")

public class Imagenes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String ruta_imagen;
    private LocalDate created_at;

    @ManyToOne
    @JoinColumn(name = "fk_formulario_id")
    private Formulario formulario_id;


}
