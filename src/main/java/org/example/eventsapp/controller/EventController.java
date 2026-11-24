package org.example.eventsapp.controller;

import lombok.RequiredArgsConstructor;
import org.example.eventsapp.dto.EventCreationDTO;
import org.example.eventsapp.dto.EventSummary;
import org.example.eventsapp.dto.PlaceDTO;
import org.example.eventsapp.dto.TicketPackDTO;
import org.example.eventsapp.model.Event;
import org.example.eventsapp.service.EventService;
import org.example.eventsapp.service.TicketService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;
    private final TicketService ticketService;

    // Головна сторінка
    @GetMapping("/")
    public String home() {
        return "redirect:/events";
    }

    // Список майбутніх подій
    @GetMapping("/events")
    public String showEvents(Model model) {

        List<EventSummary> events =
                eventService.findUpcomingEventsWithFewestFreeTickets();

        model.addAttribute("events", events);

        return "events";
    }

    // Пошук подій із вільними квитками
    @GetMapping("/events/search")
    public String searchEvents(
            @RequestParam(required = false) String title,
            Model model
    ) {
        List<EventSummary> events = Collections.emptyList();

        if (title != null && !title.isBlank()) {
            events = eventService.searchEventsWithFreeTickets(title);
        }

        model.addAttribute("events", events);
        model.addAttribute("searchedTitle", title);

        return "event/event-search";
    }

    // Сторінка окремої події
    @GetMapping("/events/{id}")
    public String showEvent(@PathVariable Long id, Model model) {
        try {
            Event event = eventService.findEventById(id);

            model.addAttribute("event", event);
            model.addAttribute(
                    "tickets",
                    ticketService.findAvailableTickets(id)
            );

            return "event/event-details";
        } catch (IllegalArgumentException e) {
            return "redirect:/events";
        }
    }

    @GetMapping("/events/new")
    public String showCreateForm(Model model) {

        EventCreationDTO dto = new EventCreationDTO();

        dto.setPlace(new PlaceDTO());

        dto.setTicketPacks(List.of(
                new TicketPackDTO(),
                new TicketPackDTO()
        ));

        model.addAttribute("eventCreationDTO", dto);

        return "event/event-create-form";
    }

    @PostMapping("/events/create")
    public String createEvent(
            @ModelAttribute EventCreationDTO dto,
            Model model
    ) {
        try {
            eventService.createEvent(dto);

            return "redirect:/events";

        } catch (IllegalArgumentException e) {

            model.addAttribute("eventCreationDTO", dto);
            model.addAttribute("error", e.getMessage());

            return "event/event-create-form";
        }
    }
}