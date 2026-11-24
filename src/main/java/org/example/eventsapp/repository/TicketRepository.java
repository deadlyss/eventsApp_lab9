package org.example.eventsapp.repository;

import jakarta.persistence.LockModeType;
import org.example.eventsapp.model.Ticket;
import org.example.eventsapp.model.TicketState;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TicketRepository extends JpaRepository<Ticket, Long> {
    @EntityGraph(attributePaths = {"event"})
    List<Ticket> findByEvent_TitleIgnoreCaseAndState(
            String title,
            TicketState state
    );

    @EntityGraph(attributePaths = {"event"})
    List<Ticket> findByEvent_IdAndStateOrderByCostAscSeatnumberAsc(
            Long eventId,
            TicketState state
    );

    @EntityGraph(attributePaths = {"event"})
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM Ticket t WHERE t.id = :id")
    Optional<Ticket> findByIdForUpdate(@Param("id") Long id);
}