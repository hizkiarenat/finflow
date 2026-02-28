package com.finflow.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.finflow.model.User;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserResponse {

    private UUID id;
    private String fullName;
    private String email;
    private String phoneNumber;
    private String status;
    private LocalDateTime createdAt;

    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    
}
