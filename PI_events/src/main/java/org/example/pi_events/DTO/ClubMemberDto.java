package org.example.pi_events.DTO;

import jakarta.validation.constraints.*;
import lombok.*;
import org.example.pi_events.entity.ClubMemberRole;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClubMemberDto {

    private Long id;

    @NotBlank
    @Size(max = 100)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    private String lastName;

    @NotBlank
    @Email
    @Size(max = 150)
    private String email;

    @Size(max = 20)
    private String phoneNumber;

    @Min(1)
    private Integer age;

    @Size(max = 100)
    private String educationLevel;

    @NotNull
    private ClubMemberRole role;

    // 🔥 IMPORTANT
    @NotNull
    private Long clubId;
}