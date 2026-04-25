package com.controlmezcla.backend.dto;

import lombok.Data;

@Data
public class ActaSiloRequest {

    private String contacto;
    private String cliente;
    private String cedula;
    private String ciudad_cedula;
    private String ciudad;
    private String obra;
    private String numero_silo;
    private String numero_toneladas;
    private String descripcion;
    private String nombre_tecnico;
    private String cedula_tecnico;
    private String fecha;
    private Long tecnico_id;

}
