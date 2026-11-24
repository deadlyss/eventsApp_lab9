package org.example.eventsapp.service;

import lombok.RequiredArgsConstructor;
import org.example.eventsapp.model.Customer;
import org.example.eventsapp.model.Ticket;
import org.example.eventsapp.model.TicketState;
import org.example.eventsapp.repository.CustomerRepository;
import org.example.eventsapp.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TicketService {

    private final TicketRepository ticketRepository;
    private final CustomerRepository customerRepository;

    @Transactional
    public Ticket purchaseTicket(Long ticketId, Long customerId) {

        // Пошук квитка з блокуванням
        Ticket ticket = ticketRepository.findByIdForUpdate(ticketId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Квиток не знайдено"));

        // Пошук покупця
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Покупця не знайдено"));

        // Перевірка статусу квитка
        if (ticket.getState() != TicketState.FREE) {
            throw new IllegalArgumentException(
                    "Цей квиток уже проданий"
            );
        }

        // Призначення квитка покупцю
        ticket.setCustomer(customer);

        // Зміна статусу
        ticket.setState(TicketState.SOLD);

        // Збереження
        return ticketRepository.save(ticket);
    }

    @Transactional(readOnly = true)
    public List<Ticket> findAvailableTickets(Long eventId) {
        return ticketRepository.findByEvent_IdAndStateOrderByCostAscSeatnumberAsc(
                eventId,
                TicketState.FREE
        );
    }
}
