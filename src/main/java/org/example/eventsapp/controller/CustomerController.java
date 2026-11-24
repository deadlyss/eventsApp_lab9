package org.example.eventsapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.example.eventsapp.dto.CustomerDTO;
import org.example.eventsapp.model.Customer;
import org.example.eventsapp.service.CustomerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping("/customers/new")
    public String showRegistrationForm(
            @RequestParam(required = false) String redirect,
            Model model
    ) {
        model.addAttribute("customerDTO", new CustomerDTO());
        model.addAttribute("redirect", redirect);

        return "auth/register-form";
    }

    @PostMapping("/customers/create")
    public String createCustomer(
            @ModelAttribute CustomerDTO dto,
            @RequestParam(required = false) String redirect,
            Model model,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        try {
            Customer customer = new Customer();

            customer.setName(dto.getName());
            customer.setEmail(dto.getEmail());
            customer.setPhone(dto.getPhone());
            customer.setPassword(dto.getPassword());

            customerService.createCustomer(customer);

            customerService.authenticateAfterRegistration(
                    dto.getEmail(),
                    dto.getPassword(),
                    request,
                    response
            );

            if (redirect != null
                    && redirect.matches("/tickets/\\d+/purchase")) {
                return "redirect:" + redirect;
            }

            return "redirect:/events";

        } catch (IllegalArgumentException e) {
            model.addAttribute("customerDTO", dto);
            model.addAttribute("redirect", redirect);
            model.addAttribute("error", e.getMessage());

            return "auth/register-form";
        }
    }
}
