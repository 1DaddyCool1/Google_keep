package com.mykola.keep.auth.service;

import com.mykola.keep.auth.dto.AuthResponse;
import com.mykola.keep.auth.dto.LoginRequest;
import com.mykola.keep.auth.dto.SignupRequest;
import com.mykola.keep.auth.entity.Role;
import com.mykola.keep.auth.entity.User;
import com.mykola.keep.auth.exception.ConflictException;
import com.mykola.keep.auth.exception.UnauthorizedException;
import com.mykola.keep.auth.repository.RoleRepository;
import com.mykola.keep.auth.util.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.mykola.keep.auth.dto.UserDto.mapUser;

@Service
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthService(PasswordEncoder passwordEncoder, RoleRepository roleRepository, UserService userService, JwtUtil jwtUtil) {
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse registerUser(SignupRequest signupRequest) {
        if(userService.existsByUsername(signupRequest.getUsername())) {
            throw new ConflictException("User already exists");
        }
        if(userService.existsByEmail(signupRequest.getEmail())) {
            throw new ConflictException("Email already exists");
        }

        User user = new User();
        user.setUsername(signupRequest.getUsername());
        user.setEmail(signupRequest.getEmail());
        user.setPasswordHash(passwordEncoder.encode(signupRequest.getPassword()));
        Role userRole = roleRepository.findByName("USER").orElseGet(() -> roleRepository.save(new Role("USER")));
        user.getRoles().add(userRole);
        user = userService.save(user);

        String token = jwtUtil.generateToken(user);
        return new AuthResponse(mapUser(user), token);
    }

    public AuthResponse loginUser(LoginRequest loginRequest) {
        User user = Optional.ofNullable(userService.findByUsername(loginRequest.getUsername()))
                .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));
        if (!user.isActive() || !passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new UnauthorizedException("Invalid credentials");
        }
        String token = jwtUtil.generateToken(user);
        return new AuthResponse(mapUser(user), token);
    }

}
