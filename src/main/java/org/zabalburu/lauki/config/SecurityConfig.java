package org.zabalburu.lauki.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
        	.authorizeHttpRequests(auth -> auth
        	    .requestMatchers("/login", "/css/**", "/js/**").permitAll()
        	    .requestMatchers("/admin/**").hasRole("ADMINISTRADOR")
        	    .requestMatchers("/tablero/**", "/grupo/**", "/api/**").hasAnyRole("ESTUDIANTE", "ADMINISTRADOR")
        	    .anyRequest().authenticated()
        	)
            .formLogin(form -> form
                .loginPage("/login")
                .successHandler((request, response, authentication) -> {
                    boolean isAdmin = authentication.getAuthorities().stream()
                            .anyMatch(a -> a.getAuthority().equals("ROLE_ADMINISTRADOR"));
                    if (isAdmin) {
                        response.sendRedirect("/admin/cursos-usuarios");
                    } else {
                        response.sendRedirect("/tablero");
                    }
                })
                .permitAll()
            )
            .logout(logout -> logout
                .logoutSuccessUrl("/login?logout")
                .permitAll()
            );

        return http.build();
    }
}