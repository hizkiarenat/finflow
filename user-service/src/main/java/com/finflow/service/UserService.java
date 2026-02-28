package com.finflow.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.finflow.dto.RegisterRequest;
import com.finflow.dto.UpdateUserRequest;
import com.finflow.dto.UserResponse;

public interface UserService {

    UserResponse register(RegisterRequest request);

    UserResponse getUserById(UUID id);

    UserResponse getUserByEmail(String email);

    UserResponse updateUser(UUID id, UpdateUserRequest request);

    void deactivateUser(UUID id);

    List<UserResponse> searchUsers(String keyword);

    Map<String, Long> getUserStatusSummary();

    List<UserResponse> findAllUser();
    
}
