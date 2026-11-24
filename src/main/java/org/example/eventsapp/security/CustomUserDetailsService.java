package org.example.eventsapp.security;

import lombok.RequiredArgsConstructor;
import org.example.eventsapp.model.Customer;
import org.example.eventsapp.repository.CustomerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final CustomerRepository customerRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Користувача з таким email не знайдено"
                        ));

        return User.builder()
                .username(customer.getEmail())
                .password(customer.getPassword())
                .roles(customer.getRole().name())
                .build();
    }
}