package com.esprit.microservice.trainingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Time;
import java.util.Date;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Session {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private Date date;
    private Time startTime;
    private int duration;
    @Enumerated(EnumType.STRING)
    private Status status;
    private int maxParticipants;
    @Column(name = "available_spots", nullable = false)
    private int availableSpots;

    @ManyToOne
    @JoinColumn(name = "training_id",nullable = false)
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Training training;

    @OneToMany(mappedBy = "session", cascade = CascadeType.ALL)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private java.util.List<Enrollment> enrollments;
}
