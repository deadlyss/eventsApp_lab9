
package org.example.eventsapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
                .authorizeHttpRequests(auth -> auth

                        // Створення подій — тільки ADMIN
                        .requestMatchers(
                                "/events/new",
                                "/events/create",
                                "/create"
                        ).hasRole("ADMIN")

                        // Доступно всім
                        .requestMatchers(
                                "/",
                                "/events",
                                "/events/*",
                                "/tickets/search",
                                "/customers/new",
                                "/customers/create",
                                "/login",
                                "/access-denied",
                                "/css/**",
                                "/js/**",
                                "/images/**"
                        ).permitAll()

                        // Купівля квитків — тільки USER
                        .requestMatchers("/tickets/*/purchase").hasRole("USER")
                        .requestMatchers("/tickets/purchase").hasRole("USER")

                        // Решта сторінок — авторизованим користувачам
                        .anyRequest().authenticated()
                )
                .securityContext(context -> context
                        .securityContextRepository(securityContextRepository())
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(authenticationSuccessHandler())
                        .failureUrl("/login?error")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/events")
                        .permitAll()
                )
                .exceptionHandling(exception -> exception
                        .accessDeniedPage("/access-denied")
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration
    ) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String redirect = request.getParameter("redirect");

            if (redirect != null
                    && redirect.matches("/tickets/\\d+/purchase")) {
                response.sendRedirect(request.getContextPath() + redirect);
            } else {
                response.sendRedirect(request.getContextPath() + "/events");
            }
        };
    }
}
