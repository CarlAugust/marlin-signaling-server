package com.example.filemarlin;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.filemarlin.repository.UserRepository;
import com.example.filemarlin.service.UserService;


@SpringBootApplication
public class App {

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder) {
        return args -> {
            // Check if test user already exists to avoid duplicate entries
            if (userRepository.findByUsername("admin").isEmpty()) {
                userService.registerUser("admin", "password");
            }
        };
    }
    
}
