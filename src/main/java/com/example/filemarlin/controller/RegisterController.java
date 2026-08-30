package com.example.filemarlin.controller;

import com.example.filemarlin.entity.User;
import com.example.filemarlin.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/register")
public class RegisterController {

    private final UserService userService;

    public RegisterController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<Void> register(@Valid @RequestBody User user) {
        String password = user.getPassword();
        String username = user.getUsername();

        userService.registerUser(username, password);

        return ResponseEntity.status(HttpStatus.SEE_OTHER)
                .location(URI.create("/login"))
                .build();
    }
}
