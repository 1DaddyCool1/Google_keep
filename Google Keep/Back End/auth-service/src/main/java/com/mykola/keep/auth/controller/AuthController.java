package com.mykola.keep.auth.controller;

import com.mykola.keep.auth.dto.AuthResponse;
import com.mykola.keep.auth.dto.LoginRequest;
import com.mykola.keep.auth.dto.SignupRequest;
import com.mykola.keep.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "Authentication related endpoints")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    public AuthController(AuthService authService) {this.authService = authService;}

    @Operation(summary = "Signup a new user", description = "Create a new user account")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request")
    })
    @PostMapping("/signup")
    public ResponseEntity<AuthResponse> signup(@RequestBody SignupRequest signupRequest) {
        return new ResponseEntity<>(authService.registerUser(signupRequest), HttpStatus.CREATED);
    }

    @Operation(summary = "Login a user", description = "Authenticate a user and retrieve JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        return new ResponseEntity<>(authService.loginUser(loginRequest), HttpStatus.OK);
    }
}
