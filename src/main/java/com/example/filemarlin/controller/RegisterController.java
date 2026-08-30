package com.example.filemarlin.controller;

import java.net.URI;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.filemarlin.dto.RegisterRequest;
import com.example.filemarlin.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        String password = request.getPassword();
        String username = request.getUsername();

        userService.registerUser(username, password);

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create("/login"))
                .build();
    }
}
