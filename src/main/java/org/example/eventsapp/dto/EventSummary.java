package org.example.eventsapp.dto;

import java.time.LocalDateTime;

public interface EventSummary {

    Long getId();

    String getTitle();

    LocalDateTime getEventDate();

    String getPlaceName();

    String getPlaceAddress();

    Long getAvailableTickets();
}