package org.example.eventsapp.dto;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class EventCreationDTO {
    private String title;
    private LocalDateTime eventDate;
    private List<TicketPackDTO> ticketPacks;
    private PlaceDTO place;
}
