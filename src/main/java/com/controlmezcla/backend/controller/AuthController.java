package com.controlmezcla.backend.controller;

import com.controlmezcla.backend.dto.LoginRequest;
import com.controlmezcla.backend.dto.LoginResponse;
import com.controlmezcla.backend.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authservice;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login (@RequestBody LoginRequest request)
    {
        try
        {
            LoginResponse response = authservice.login(request);
            return ResponseEntity.ok(response);
        }
        catch (RuntimeException e)
        {
            return ResponseEntity.status(401).build();
        }
    }

}
