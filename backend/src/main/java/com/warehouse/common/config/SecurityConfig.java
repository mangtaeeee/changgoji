package com.warehouse.common.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    private static final String[] SWAGGER_PATHS = {
        "/swagger-ui.html",
        "/swagger-ui/**",
        "/v3/api-docs/**"
    };

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .httpBasic(Customizer.withDefaults())
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers(SWAGGER_PATHS).hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/v1/**").hasAnyRole("ADMIN", "OPERATOR")
                .requestMatchers("/api/v1/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService(
        @Value("${app.security.username}") String username,
        @Value("${app.security.password}") String password,
        PasswordEncoder passwordEncoder
    ) {
        UserDetails admin = User.builder()
            .username(username)
            .password(passwordEncoder.encode(password))
            .roles("ADMIN", "OPERATOR")
            .build();
        UserDetails operator = User.builder()
            .username("operator")
            .password(passwordEncoder.encode(password))
            .roles("OPERATOR")
            .build();
        return new InMemoryUserDetailsManager(List.of(admin, operator));
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
