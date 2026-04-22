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
public class EventParticipationDTO {

    private Long id;
    private LocalDate participationDate;
    private String status;

    private String fullName;
    private String email;
    private String phone;
    private String university;
    private String level;
    private String motivation;

    private Long eventId;
}