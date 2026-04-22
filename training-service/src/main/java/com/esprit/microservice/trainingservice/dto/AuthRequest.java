package com.esprit.microservice.trainingservice.dto;

import lombok.Data;

@Data
public class AuthRequest {
    private String email;
    private String password;
}
