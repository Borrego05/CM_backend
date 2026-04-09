package com.controlmezcla.backend.dto;

import lombok.Data;

@Data
public class LoginRequest {

    private String usuario;
    private String pwd;
}
