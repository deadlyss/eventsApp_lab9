package org.example.eventsapp.service;

import lombok.RequiredArgsConstructor;
import org.example.eventsapp.dto.EventCreationDTO;
import org.example.eventsapp.dto.TicketPackDTO;
import org.example.eventsapp.model.*;
import org.example.eventsapp.repository.EventRepository;
import org.example.eventsapp.repository.PlaceRepository;
import org.example.eventsapp.repository.TicketRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.example.eventsapp.dto.EventSummary;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final PlaceRepository placeRepository;
    private final TicketRepository ticketRepository;

    // Пошук майбутніх подій із вільними квитками за назвою
    @Transactional(readOnly = true)
    public List<EventSummary> searchEventsWithFreeTickets(String title) {

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException(
                    "Необхідно вказати назву події"
            );
        }

        return eventRepository.searchEventsWithFreeTickets(
                title.trim(),
                LocalDateTime.now(),
                TicketState.FREE
        );
    }

    // Пошук найближчих майбутніх подій
    @Transactional(readOnly = true)
    public List<Event> findUpcomingEvents() {
        return eventRepository.findByEventDateAfterOrderByEventDateAsc(
                LocalDateTime.now()
        );
    }

    // 10 майбутніх подій із найменшою кількістю вільних квитків
    @Transactional(readOnly = true)
    public List<EventSummary> findUpcomingEventsWithFewestFreeTickets() {
        return eventRepository.findUpcomingEventsWithFewestFreeTickets(
                LocalDateTime.now(),
                TicketState.FREE,
                PageRequest.of(0, 10)
        );
    }

    // Пошук за id
    @Transactional(readOnly = true)
    public Event findEventById(Long id) {
        return eventRepository.findWithPlaceById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException("Подію не знайдено")
                );
    }

    @Transactional
    public Event createEvent(EventCreationDTO dto) {

        // Перевірка вхідних даних
        if (dto == null || dto.getPlace() == null) {
            throw new IllegalArgumentException("Дані події або місця не вказані");
        }

        if (dto.getTitle() == null || dto.getTitle().isBlank()) {
            throw new IllegalArgumentException("Назва події не може бути порожньою");
        }

        if (dto.getEventDate() == null ||
                !dto.getEventDate().isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Дата події повинна бути в майбутньому");
        }

        if (dto.getTicketPacks() == null || dto.getTicketPacks().isEmpty()) {
            throw new IllegalArgumentException("Необхідно додати хоча б один набір квитків");
        }

        // Пошук або створення місця
        String placeName = dto.getPlace().getName();

        if (placeName == null || placeName.isBlank()) {
            throw new IllegalArgumentException("Назва місця не може бути порожньою");
        }

        Place place = placeRepository.findByName(placeName)
                .orElseGet(() -> {
                    if (dto.getPlace().getAddress() == null ||
                            dto.getPlace().getAddress().isBlank()) {
                        throw new IllegalArgumentException("Адреса місця не вказана");
                    }

                    Place newPlace = new Place();
                    newPlace.setName(placeName);
                    newPlace.setAddress(dto.getPlace().getAddress());

                    return placeRepository.save(newPlace);
                });

        // Перевірка зайнятості місця в цей день
        LocalDateTime startOfDay = dto.getEventDate().toLocalDate().atStartOfDay();
        LocalDateTime startOfNextDay = startOfDay.plusDays(1);

        boolean eventExists = eventRepository
                .existsByPlace_IdAndEventDateGreaterThanEqualAndEventDateLessThan(
                        place.getId(),
                        startOfDay,
                        startOfNextDay
                );

        if (eventExists) {
            throw new IllegalArgumentException(
                    "У цьому місці вже запланована подія на вказану дату"
            );
        }

        // Створення події
        Event event = new Event();
        event.setTitle(dto.getTitle());
        event.setEventDate(dto.getEventDate());
        event.setPlace(place);

        eventRepository.save(event);

        // Створення квитків
        List<Ticket> tickets = new ArrayList<>();
        long seatNumber = 1;

        for (TicketPackDTO pack : dto.getTicketPacks()) {

            if (pack.getCost() == null ||
                    pack.getCost().signum() <= 0) {
                throw new IllegalArgumentException("Ціна квитка повинна бути більшою за 0");
            }

            if (pack.getCount() <= 0) {
                throw new IllegalArgumentException("Кількість квитків повинна бути більшою за 0");
            }

            for (int i = 0; i < pack.getCount(); i++) {

                Ticket ticket = new Ticket();
                ticket.setCost(pack.getCost());
                ticket.setSeatnumber(seatNumber++);
                ticket.setState(TicketState.FREE);
                ticket.setEvent(event);

                tickets.add(ticket);
            }
        }

        // Збереження всіх квитків
        ticketRepository.saveAll(tickets);

        return event;
    }
}
