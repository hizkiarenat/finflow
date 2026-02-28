package com.finflow.dto;

import java.util.UUID;

import lombok.Data;

//  * DTO untuk menerima response dari User Service via Feign Client
@Data
public class UserResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String status;
}
