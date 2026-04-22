package com.esprit.microservice.trainingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDto {
    private String userId;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    
    public String getDisplayName() {
        if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
            return firstName + " " + lastName;
        }
        if (firstName != null && !firstName.isEmpty()) {
            return firstName;
        }
        return username;
    }
}
