package org.example.eventsapp.repository;

import org.example.eventsapp.model.Event;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.example.eventsapp.dto.EventSummary;
import org.example.eventsapp.model.TicketState;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByPlace_IdAndEventDateGreaterThanEqualAndEventDateLessThan(
            Long placeId,
            LocalDateTime startOfDay,
            LocalDateTime startOfNextDay
    );
    @EntityGraph(attributePaths = {"place"})
    List<Event> findByEventDateAfterOrderByEventDateAsc(
            LocalDateTime date
    );

    @EntityGraph(attributePaths = {"place"})
    Optional<Event> findWithPlaceById(Long id);

    @Query("""
    SELECT
        e.id AS id,
        e.title AS title,
        e.eventDate AS eventDate,
        p.name AS placeName,
        p.address AS placeAddress,
        COUNT(t.id) AS availableTickets
    FROM Event e
    JOIN e.place p
    LEFT JOIN e.tickets t ON t.state = :state
    WHERE e.eventDate > :now
    AND LOWER(e.title) LIKE LOWER(CONCAT('%', :title, '%'))
    GROUP BY e.id, e.title, e.eventDate, p.name, p.address
    HAVING COUNT(t.id) > 0
    ORDER BY e.eventDate ASC
    """)
    List<EventSummary> searchEventsWithFreeTickets(
            @Param("title") String title,
            @Param("now") LocalDateTime now,
            @Param("state") TicketState state
    );

    @Query("""
        SELECT
            e.id AS id,
            e.title AS title,
            e.eventDate AS eventDate,
            p.name AS placeName,
            p.address AS placeAddress,
            COUNT(t.id) AS availableTickets
        FROM Event e
        JOIN e.place p
        LEFT JOIN e.tickets t ON t.state = :state
        WHERE e.eventDate > :now
        GROUP BY e.id, e.title, e.eventDate, p.name, p.address
        HAVING COUNT(t.id) > 0
        ORDER BY COUNT(t.id) ASC, e.eventDate ASC
        """)
    List<EventSummary> findUpcomingEventsWithFewestFreeTickets(
            @Param("now") LocalDateTime now,
            @Param("state") TicketState state,
            Pageable pageable
    );
}
