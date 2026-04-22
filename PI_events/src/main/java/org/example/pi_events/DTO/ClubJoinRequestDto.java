package org.example.pi_events.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.example.pi_events.entity.ClubJoinRequestStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubJoinRequestDto {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private ClubJoinRequestStatus status;
    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private Long clubId;
}
