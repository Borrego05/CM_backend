package com.controlmezcla.backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JWTFilter extends OncePerRequestFilter {

    @Autowired
    private JWTUtil jwtutil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterchain) throws ServletException, IOException
    {
        String header = request.getHeader("Authorization");

        System.out.println("==== JWT FILTER ====");
        System.out.println("Header Authorization: " + header);
        System.out.println("URL: " + request.getRequestURI());

        if (header != null && header.startsWith("Bearer "))
        {
            String token = header.substring(7);
            System.out.println("Token extraido: " + token);
            System.out.println("Token valido: " + jwtutil.validarToken(token));

            if (jwtutil.validarToken(token))
            {
                String usuario = jwtutil.obtenerUsuario(token);
                String rol = jwtutil.obtenerRol(token);
                System.out.println("Usuario: " + usuario + "rol: " + rol);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                        usuario,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + rol))  // <- faltaba _
                );

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        }

        System.out.println("Autentication: " + SecurityContextHolder.getContext().getAuthentication());
        filterchain.doFilter(request, response);
    }
}