package com.mykola.keep.auth.dto;

import com.mykola.keep.auth.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDto {
    private String id;
    private String username;
    private String email;

    public static UserDto mapUser(User user) {
        return new UserDto(String.valueOf(user.getId()), user.getUsername(), user.getEmail());
    }
}
