package org.example.pi_events.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EventFeedbackDTO {

    private Long id;
    private Integer rating;
    private String comment;
    private LocalDate submittedAt;
    private Long eventId;
}
