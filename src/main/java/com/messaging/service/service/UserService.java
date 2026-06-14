package com.messaging.service.service;

import com.messaging.service.api.dto.AuthResponse;
import com.messaging.service.domain.MyUser;
import com.messaging.service.repository.UserRepository;
import com.messaging.service.security.JwtProperties;
import com.messaging.service.security.JwtService;
import org.springframework.stereotype.Service;
import com.messaging.service.api.dto.LoginRequest;
import com.messaging.service.api.dto.RegisterRequest;
import com.messaging.service.api.exception.UsernameTakenException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.time.Instant;

@Service
public class UserService {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;

    public UserService(UserRepository userRepo, PasswordEncoder passwordEncoder, JwtService jwtService, JwtProperties jwtProperties) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
    }

    public MyUser register(RegisterRequest req) {
        if (userRepo.existsByUsername(req.username()))
            throw new UsernameTakenException(req.username());      // → 409
        return userRepo.save(MyUser.builder()
                .username(req.username())
                .passwordHash(passwordEncoder.encode(req.password()))
                .build());
    }

    public AuthResponse login(LoginRequest req) {
        MyUser user = userRepo.findByUsername(req.username())
                .orElseThrow(() -> new BadCredentialsException("Bad credentials")); // → 401
        if (!passwordEncoder.matches(req.password(), user.getPasswordHash()))
            throw new BadCredentialsException("Bad credentials");
        String token = jwtService.issue(user.getId(), user.getUsername());
        return new AuthResponse(token, Instant.now().plusSeconds(jwtProperties.getTtlMinutes() * 60));
    }
}
