package org.example.eventsapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.eventsapp.model.Customer;
import org.example.eventsapp.model.Ticket;
import org.example.eventsapp.service.CustomerService;
import org.example.eventsapp.service.TicketService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class TicketController {

    private final CustomerService customerService;
    private final TicketService ticketService;

    @GetMapping("/tickets/{id}/purchase")
    public String showPurchaseForm(
            @PathVariable Long id,
            Model model
    ) {
        model.addAttribute("ticketId", id);
        return "ticket/purchase-form";
    }


    @PostMapping("/tickets/purchase")
    public String purchaseTicket(
            @RequestParam Long ticketId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model
    ) {
        try {
            Customer customer = customerService.findByEmail(
                    userDetails.getUsername()
            );

            Ticket ticket = ticketService.purchaseTicket(
                    ticketId,
                    customer.getId()
            );

            model.addAttribute("ticket", ticket);
            return "ticket/purchase-success";

        } catch (IllegalArgumentException e) {
            model.addAttribute("ticketId", ticketId);
            model.addAttribute("error", e.getMessage());
            return "ticket/purchase-form";
        }
    }
}
