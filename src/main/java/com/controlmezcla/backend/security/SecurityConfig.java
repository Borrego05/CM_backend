package com.controlmezcla.backend.security;

import com.controlmezcla.backend.model.Usuario;
import com.controlmezcla.backend.repository.UsuarioRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JWTFilter jwtfilter;
    private final UsuarioRepository usuario_repository;

    public SecurityConfig(JWTFilter jwtfilter, UsuarioRepository usuario_repository)

    {
        this.jwtfilter = jwtfilter;
        this.usuario_repository = usuario_repository;
    }

    @Bean
    public PasswordEncoder pwdencoder()
    {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception
    {
        http.csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtfilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                        {
                            System.out.println("==== ACCESS DENIED ====");
                            System.out.println("Razon: " + accessDeniedException.getMessage());
                            System.out.println("URL: " + request.getRequestURI());
                            response.sendError(403, accessDeniedException.getMessage());
                        })
                        .authenticationEntryPoint((request, response, authException) ->
                        {
                            System.out.println("==== AUTH ENTRY POINT ====");
                            System.out.println("Razon: " + authException.getMessage());
                            response.sendError(401, authException.getMessage());
                        }));

        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService()
    {
        return username ->
        {
            Usuario usuario = usuario_repository.findByUsuario(username)
                    .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

            return org.springframework.security.core.userdetails.User
                    .withUsername(usuario.getUsuario())
                    .password(usuario.getPwd())
                    .authorities("ROLE_" + usuario.getRol())
                    .build();

        };
    }

    @Bean
    public AuthenticationManager authenticationManager (AuthenticationConfiguration config) throws Exception
    {
        return config.getAuthenticationManager();
    }

}
