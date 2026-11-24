package org.example.eventsapp.dto;

import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class TicketPackDTO {
    private BigDecimal cost;
    private int count;
}
