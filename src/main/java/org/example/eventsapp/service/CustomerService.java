
package org.example.eventsapp.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.eventsapp.model.Customer;
import org.example.eventsapp.model.Role;
import org.example.eventsapp.repository.CustomerRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    @Transactional
    public Customer createCustomer(Customer customer) {

        if (customer == null) {
            throw new IllegalArgumentException("Дані покупця не вказані");
        }

        if (customer.getName() == null || customer.getName().isBlank()) {
            throw new IllegalArgumentException("Ім'я покупця не вказане");
        }

        if (customer.getEmail() == null || customer.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email покупця не вказаний");
        }

        if (customer.getPassword() == null ||
                customer.getPassword().isBlank()) {
            throw new IllegalArgumentException("Пароль не вказаний");
        }

        if (customerRepository.existsByEmail(customer.getEmail())) {
            throw new IllegalArgumentException(
                    "Покупець із таким email вже існує"
            );
        }

        customer.setPassword(
                passwordEncoder.encode(customer.getPassword())
        );

        customer.setRole(Role.USER);

        return customerRepository.save(customer);
    }

    public void authenticateAfterRegistration(
            String email,
            String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                password
                        )
                );

        SecurityContext context =
                SecurityContextHolder.createEmptyContext();

        context.setAuthentication(authentication);

        SecurityContextHolder.setContext(context);

        securityContextRepository.saveContext(
                context,
                request,
                response
        );
    }

    @Transactional(readOnly = true)
    public Customer findByEmail(String email) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Необхідно вказати email");
        }

        return customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Покупця з таким email не знайдено"
                        ));
    }
}
