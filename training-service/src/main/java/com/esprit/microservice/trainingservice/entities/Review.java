package com.esprit.microservice.trainingservice.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(uniqueConstraints = {
    @UniqueConstraint(columnNames = {"training_id", "student_id"})
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int rating; // 1-5
    
    @Column(length = 1000)
    private String comment;

    private LocalDateTime createdAt;

    @JsonProperty("student")
    @Column(name = "student_id")
    private Long studentId;

    @ManyToOne
    @JoinColumn(name = "training_id")
    @org.hibernate.annotations.OnDelete(action = org.hibernate.annotations.OnDeleteAction.CASCADE)
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Training training;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
