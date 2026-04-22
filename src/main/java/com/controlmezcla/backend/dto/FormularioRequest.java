package com.controlmezcla.backend.dto;

import lombok.Data;

@Data
public class FormularioRequest {

    private String cliente;
    private String direccion;
    private String obra;
    private String telefono;
    private String fecha;
    private String descripcion;
    private String materiales_utilizados;
    private String clases_mantenimiento;
    private String tipo_mantenimiento;
    private String contacto; //Ultimo campo agregado
    private Long fk_tecnico_id;


}
