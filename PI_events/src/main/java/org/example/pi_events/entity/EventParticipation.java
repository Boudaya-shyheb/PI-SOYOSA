package org.example.pi_events.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "event_participation")
public class EventParticipation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate participationDate;

    @Column(nullable = false)
    private String status;

    // 🔥 NEW FIELDS
    @Column(nullable = false)
    private String fullName;

    @Column(nullable = false)
    private String email;

    private String phone;

    private String university;

    private String level;

    @Column(length = 1000)
    private String motivation;

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;
}