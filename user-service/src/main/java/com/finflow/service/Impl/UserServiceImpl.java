package com.finflow.service.Impl;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.finflow.dto.RegisterRequest;
import com.finflow.dto.UpdateUserRequest;
import com.finflow.dto.UserResponse;
import com.finflow.dto.UserStatusCount;
import com.finflow.exception.DuplicateResourceException;
import com.finflow.exception.UserNotFoundException;
import com.finflow.model.User;
import com.finflow.repository.UserRepository;
import com.finflow.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String CACHE_BY_ID = "user:id:";
    private static final String CACHE_BY_EMAIL = "user:email:";
    private static final Duration CACHE_TTL = Duration.ofMinutes(10);

    // createUser
    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // validasi duplikasi email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered! " + request.getEmail());
        }
        // validasi phoneNumber
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())
                && request.getPhoneNumber() != null) {
            throw new DuplicateResourceException("Phonenumber already registered! " + request.getPhoneNumber());
        }

        User userNew = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase())
                .phoneNumber(request.getPhoneNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .status(User.UserStatus.ACTIVE)
                .createdAt(LocalDateTime.now())
                .build();

        User savedUser = userRepository.save(userNew);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        return UserResponse.fromEntity(savedUser);
    }

    // updateUser
    @Override
    @Transactional
    public UserResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        if (request.getFullName() != null) {
            user.setFullName(request.getFullName());
            if (request.getPhoneNumber() != null) {
                if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
                    throw new DuplicateResourceException("Phonenumber already in use!");
                }
                user.setPhoneNumber(request.getPhoneNumber());
            }
        }
        UserResponse response = UserResponse.fromEntity(userRepository.save(user));

        // Invalidate cache karena data user sudah berubah
        invalidateCache(id, user.getEmail());

        return response;
    }

    // deleteUser
    @Override
    @Transactional
    public void deactivateUser(UUID id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        user.setStatus(User.UserStatus.INACTIVE);
        userRepository.save(user);

        // invalidate cache karena status user sudah berubah
        invalidateCache(id, user.getEmail());
    }

    // getUserByEmail
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        
        // cek redis dulu
        String cacheKey = CACHE_BY_EMAIL + email;
        UserResponse cached = (UserResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Cache HIT - email: {}", email);
            return cached;
        }

        // cache MISS - query to DB
        User user = userRepository.findByEmail(email.toLowerCase())
                .orElseThrow(() -> new UserNotFoundException("User not found with email: " + email));
        UserResponse response = UserResponse.fromEntity(user);

        // simpan ke Redis dengan TTL
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
        log.info("Cache SET -    email: {}", email);
        return response;
    }

    // getUserById
    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id) {

        // cek Redis dulu
        String cacheKey = CACHE_BY_ID + id;
        UserResponse cached = (UserResponse) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            log.info("Cache HIT - userId: {}", id);
            return cached;
        }

        // cache MISS - query to DB
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
        UserResponse response =  UserResponse.fromEntity(user);

        // simpan ke Redis dengan TTL
        redisTemplate.opsForValue().set(cacheKey, response, CACHE_TTL);
        log.info("CACHE SET - userId: {}", id);
        return response;
    }

    // getUserSummary
    @Override
    @Transactional(readOnly = true)
    public Map<String, Long> getUserStatusSummary() {
        return userRepository.countUsersByStatus()
                .stream()
                .collect(Collectors.toMap(
                        UserStatusCount::getStatus,
                        UserStatusCount::getTotal));
    }

    // getUserByEmail/Name
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> searchUsers(String keyword) {
        log.debug("Searching users with keyword: {}", keyword);

        return userRepository.searchActiveUser(keyword)
                .stream()
                .filter(user -> user.getStatus() == User.UserStatus.ACTIVE)
                .map(UserResponse::fromEntity)
                .sorted(Comparator.comparing(UserResponse::getFullName))
                .collect(Collectors.toList());
    }

    // getAllData
    @Override
    @Transactional(readOnly = true)
    public List<UserResponse> findAllUser() {
        List<User> users = userRepository.findAllUser();
        if (users.isEmpty()) {
            throw new UserNotFoundException("User Data is empty!");
        }
        return userRepository.findAllUser()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .status(user.getStatus().name())
                .createdAt(user.getCreatedAt())
                .build();
    }

    // -------------------------------------------------------
    // Helper: invalidate semua cache yang berkaitan dengan rekening (entah credit/debit)
    // -------------------------------------------------------
    private void invalidateCache(UUID id, String email) {
        redisTemplate.delete(CACHE_BY_ID + id);
        redisTemplate.delete(CACHE_BY_EMAIL + email);
        log.info("Cache invalidated for userId: {}", id);
    }
}
