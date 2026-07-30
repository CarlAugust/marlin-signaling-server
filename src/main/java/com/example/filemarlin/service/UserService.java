package com.example.filemarlin.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.filemarlin.entity.User;
import com.example.filemarlin.repository.UserRepository;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(String username, String password) {
        if (!userRepository.findByUsername(username).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Username is already taken");
        }

        String encodedPassword = passwordEncoder.encode(password);
        User user = new User(null, username, encodedPassword);
        userRepository.save(user);
    }
}
