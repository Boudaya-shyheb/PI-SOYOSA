package org.example.pi_events.DTO;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubDto {

    private Long id;

    @NotBlank
    @Size(max = 255)
    private String name;

    @Size(max = 1000)
    private String description;

    @Size(max = 255)
    private String meetingLocation;

    @Size(max = 255)
    private String meetingSchedule;

    @Min(1)
    private Integer maxMembers;

    private LocalDate createdAt;
}
