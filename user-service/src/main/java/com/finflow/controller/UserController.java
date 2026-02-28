package com.finflow.controller;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.catalina.connector.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.finflow.dto.ApiResponse;
import com.finflow.dto.RegisterRequest;
import com.finflow.dto.UpdateUserRequest;
import com.finflow.dto.UserResponse;
import com.finflow.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest registerRequest) {
        UserResponse response = userService.register(registerRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("User registered successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success("User Found!", response));
    }

    @GetMapping("/byEmail")
    public ResponseEntity<ApiResponse<UserResponse>> getUserByEmail(@RequestParam String email) {
        UserResponse response = userService.getUserByEmail(email);
        return ResponseEntity.ok(ApiResponse.success("User Found!", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(@Valid @PathVariable UUID id,
            @RequestBody UpdateUserRequest updateRequest) {
        UserResponse response = userService.updateUser(id, updateRequest);
        return ResponseEntity.ok(ApiResponse.success("User Updated successfully!", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> deleteUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok(ApiResponse.success("User deactivated", null));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<UserResponse>>> searchUsers(@RequestParam String keyword) {
        List<UserResponse> resuList = userService.searchUsers(keyword);
        return ResponseEntity.ok(ApiResponse.success(
                "Found " + resuList.size() + " users", resuList));
    }

    @GetMapping("/stats/status")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUserStatusSummary() {
        Map<String, Long> summary = userService.getUserStatusSummary();
        return ResponseEntity.ok(ApiResponse.success("User status summary", summary));
    }

    @GetMapping("/findAllUser")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUser() {
        return ResponseEntity.ok(ApiResponse.success("All users", userService.findAllUser()));
    }
}
