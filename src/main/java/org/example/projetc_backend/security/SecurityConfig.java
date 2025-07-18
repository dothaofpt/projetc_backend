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
                        .requestMatchers(HttpMethod.GET, "/api/lessons/**").permitAll()

                        // ====================================================================
                        // BỔ SUNG QUY TẮC CHO CÁC API PRACTICE ACTIVITIES
                        // ====================================================================
                        // Cho phép truy cập GET tới tất cả các hoạt động luyện tập cho USER và ADMIN
                        // Hoặc bạn có thể chọn permitAll() nếu muốn công khai hoàn toàn
                        .requestMatchers(HttpMethod.GET, "/api/practice-activities/**").hasAnyRole("USER", "ADMIN")
                        // Hoặc nếu bạn muốn hoàn toàn công khai, không cần đăng nhập để xem danh sách:
                        // .requestMatchers(HttpMethod.GET, "/api/practice-activities/**").permitAll()


                        // Các API POST, PUT, DELETE cho hoạt động luyện tập chỉ dành cho ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/practice-activities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/practice-activities/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/practice-activities/**").hasRole("ADMIN")

                        // ====================================================================
                        // KẾT THÚC BỔ SUNG
                        // ====================================================================

                        // Existing rules:
                        .requestMatchers(HttpMethod.POST, "/api/lessons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/lessons/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/lessons/**").hasRole("ADMIN")
                        .requestMatchers("/api/stats").hasRole("ADMIN")
                        .requestMatchers("/api/questions/**", "/api/answers/**", "/api/learning-materials/**").hasRole("ADMIN")
                        .requestMatchers("/api/payments/paypal/complete", "/api/payments/paypal/cancel").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.POST, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.PUT, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER")
                        .requestMatchers(HttpMethod.DELETE, "/api/flashcard-sets/**").hasAnyRole("ADMIN", "USER")
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
                "http://localhost:4200",
                "http://localhost:8000",
                "http://localhost:8080",
                "http://localhost:61299",
                "http://192.168.2.12:8080",
                "http://10.24.27.184:8080"
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