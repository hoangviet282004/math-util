package com.project.KoiBookingSystem.model.response;

import com.project.KoiBookingSystem.enums.Role;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class LoginResponse {

    private String username;
    private String userID;
    private String phone;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    private String token;
}
