package com.mykola.keep.auth.service;

import com.mykola.keep.auth.entity.User;
import com.mykola.keep.auth.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {this.userRepository = userRepository;}

    public User findByUsername(String username) {return userRepository.findByUsername(username).orElse(null);}

    public User findByEmail(String email) {return userRepository.findByEmail(email).orElse(null);}

    public boolean existsByUsername(String username) {return userRepository.existsByUsername(username);}

    public boolean existsByEmail(String email) { return userRepository.existsByEmail(email); }

    public User save(User appUser) {
        return userRepository.save(appUser);
    }

    public Optional<User> findUserById(Long id) {
        return userRepository.findById(id);
    }
}
