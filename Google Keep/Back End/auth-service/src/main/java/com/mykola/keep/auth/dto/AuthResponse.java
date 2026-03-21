package com.mykola.keep.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private UserDto user;
    private String token;
    private String message;
    public AuthResponse(UserDto user, String token) {this.user = user; this.token = token;}
}
