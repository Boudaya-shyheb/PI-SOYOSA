package com.esprit.microservice.trainingservice.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long studentId;
    
    @Column(length = 500)
    private String message;
    
    private String type; // e.g., "CERTIFICATE", "INFO"
    private Long referenceId; // e.g., trainingId
    
    @com.fasterxml.jackson.annotation.JsonProperty("isRead")
    private boolean isRead = false;
    
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
