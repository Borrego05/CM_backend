package com.controlmezcla.backend.service;

import com.controlmezcla.backend.dto.LoginRequest;
import com.controlmezcla.backend.dto.LoginResponse;
import com.controlmezcla.backend.model.Usuario;
import com.controlmezcla.backend.repository.UsuarioRepository;
import com.controlmezcla.backend.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UsuarioRepository usuariorepository;

    @Autowired
    private JWTUtil jwtutil;

    @Autowired
    private PasswordEncoder pwdencoder;

    public LoginResponse login(LoginRequest request)
    {
        Usuario usuario = usuariorepository.findByUsuario(request.getUsuario())
                .orElseThrow( () -> new RuntimeException(("Usuario no encontrado")));

        if(!pwdencoder.matches(request.getPwd(), usuario.getPwd()))
        {
            throw new RuntimeException("Contraseña incorrecta");
        }

        String token = jwtutil.generarToken(usuario.getUsuario(), usuario.getRol());

        return new LoginResponse(token, usuario.getUsuario(), usuario.getRol());
    }
}
