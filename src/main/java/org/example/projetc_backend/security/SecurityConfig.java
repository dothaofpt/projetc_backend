package org.example.projetc_backend.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints (no authentication required)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/quizzes/**", "/api/vocabulary/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll() // Cho phép GET cho tất cả
                        .requestMatchers(HttpMethod.POST, "/api/lessons/**").hasRole("ADMIN") // Giới hạn POST cho ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/lessons/**").hasRole("ADMIN") // Giới hạn PUT cho ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/api/lessons/**").hasRole("ADMIN") // Giới hạn DELETE cho ADMIN
                        .requestMatchers("/api/stats").hasRole("ADMIN")
                        .requestMatchers("/api/questions/**", "/api/answers/**", "/api/learning-materials/**").hasRole("ADMIN")

                        // Add flashcard-sets endpoints
                        .requestMatchers(HttpMethod.GET, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER") // Allow GET for authenticated users
                        .requestMatchers(HttpMethod.POST, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER") // Allow POST for authenticated users
                        .requestMatchers(HttpMethod.PUT, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER") // Allow PUT for authenticated users
                        .requestMatchers(HttpMethod.DELETE, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER") // Allow DELETE for authenticated users

                        // Protected endpoints
                        .requestMatchers("/api/progress/**", "/api/quiz-results/**", "/api/user-flashcards/**")
                        .hasAnyRole("ADMIN", "USER")

                        // All other requests require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:4200", // Angular default port
                "http://localhost:8000",
                "http://localhost:8080", // Backend port
                "http://localhost:61299"
        ));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}