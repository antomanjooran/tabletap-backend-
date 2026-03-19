package com.tabletap.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.cors-origins}")
    private String corsOrigins;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .cors(c -> c.configurationSource(corsConfigurationSource()))
                .authorizeHttpRequests(auth -> auth
                        // WebSocket
                        .requestMatchers("/ws/**").permitAll()
                        // Menu
                        .requestMatchers(HttpMethod.GET,    "/api/menu").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/menu/**").permitAll()
                        // Tables
                        .requestMatchers(HttpMethod.GET,    "/api/tables/**").permitAll()
                        // Orders - customer + kitchen dashboard
                        .requestMatchers(HttpMethod.POST,   "/api/orders").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/orders").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/orders/**").permitAll()
                        .requestMatchers(HttpMethod.PATCH,  "/api/orders/**").permitAll()
                        // Waiter calls
                        .requestMatchers(HttpMethod.POST,   "/api/waiter/calls").permitAll()
                        .requestMatchers(HttpMethod.GET,    "/api/waiter/calls/**").permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/waiter/calls/**").permitAll()
                        // Payments
                        .requestMatchers(HttpMethod.POST,   "/api/payments/intent").permitAll()
                        .requestMatchers(HttpMethod.POST,   "/api/payments/webhook").permitAll()
                        // Health
                        .requestMatchers("/actuator/health").permitAll()
                        .anyRequest().authenticated()
                );
        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(corsOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}