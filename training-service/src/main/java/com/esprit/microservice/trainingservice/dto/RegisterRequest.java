package com.esprit.microservice.trainingservice.dto;

import com.esprit.microservice.trainingservice.entities.Role;
import lombok.Data;

@Data
public class RegisterRequest {
    private String name;
    private String email;
    private String password;
    private Role role; // STUDENT | TUTOR | ADMIN
}
