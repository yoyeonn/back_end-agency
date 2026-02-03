package com.travel.security;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    public SecurityConfig(JwtAuthFilter jwtAuthFilter) {
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http
            .cors(cors -> {})
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // auth
                .requestMatchers("/api/auth/signup", "/api/auth/login", "/api/auth/forgot-password", "/api/auth/reset-password").permitAll()
                .requestMatchers("/api/auth/change-password").authenticated()
                .requestMatchers("/api/auth/me").authenticated()

                // public browse
                .requestMatchers(HttpMethod.GET, "/api/hotels/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/destinations/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/packs/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/contact").permitAll()

                // reservations (must be logged in)
                .requestMatchers(HttpMethod.POST, "/api/reservations").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/*/invoice").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/reservations/*/invoice.pdf").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/destination-reservations").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/destination-reservations/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/destination-reservations/*/invoice").authenticated()
                .requestMatchers(HttpMethod.POST, "/api/pack-reservations").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/pack-reservations/me").authenticated()
                .requestMatchers(HttpMethod.GET, "/api/pack-reservations/*/invoice").authenticated()

                // admin reservations endpoints
                .requestMatchers("/api/admin/reservations/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/dashboard/**").hasRole("ADMIN")
                .requestMatchers("/api/admin/profile/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/hotels/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/hotels/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/hotels/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/destinations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/destinations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/destinations/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/packs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/packs/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE,"/api/packs/**").hasRole("ADMIN")

                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:4200"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
