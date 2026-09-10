package com.sentinelcare.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sentinelcare.security.JwtAuthenticationFilter;
import com.sentinelcare.web.ApiProblemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
                                            JwtAuthenticationFilter jwtAuthenticationFilter,
                                            ApiProblemWriter problemWriter) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, ex) ->
                    problemWriter.write(response, HttpStatus.UNAUTHORIZED, "Unauthorized",
                        "Authentication required to access this resource."))
                .accessDeniedHandler((request, response, ex) ->
                    problemWriter.write(response, HttpStatus.FORBIDDEN, "Forbidden",
                        "You do not have permission to perform this action.")))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/health").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/v1/auth/login").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/v1/patients/**").hasAnyRole("ADMIN", "CLINICIAN")
                .requestMatchers(HttpMethod.POST, "/api/v1/patients").hasRole("ADMIN")
                .requestMatchers("/api/v1/consents/**").hasAnyRole("ADMIN", "CLINICIAN")
                .requestMatchers("/api/v1/consult-notes/**").hasAnyRole("ADMIN", "CLINICIAN")
                .requestMatchers(HttpMethod.GET, "/api/v1/audit").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/audit/**").hasAnyRole("ADMIN", "CLINICIAN")
                .requestMatchers("/api/v1/gdpr/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        return http.build();
    }

    @Bean
    public ApiProblemWriter apiProblemWriter(ObjectMapper objectMapper) {
        return new ApiProblemWriter(objectMapper);
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile({"demo", "local", "test"})
    public UserDetailsService userDetailsService(PasswordEncoder passwordEncoder) {
        UserDetails admin = User.withUsername("admin")
            .password(passwordEncoder.encode("admin"))
            .roles("ADMIN")
            .build();

        UserDetails clinician = User.withUsername("clinician")
            .password(passwordEncoder.encode("clinician"))
            .roles("CLINICIAN")
            .build();

        return new InMemoryUserDetailsManager(admin, clinician);
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }
}