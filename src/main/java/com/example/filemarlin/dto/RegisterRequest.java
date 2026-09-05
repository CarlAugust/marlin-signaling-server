package com.example.filemarlin.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public class RegisterRequest {
    @NotEmpty
    @NotNull
    private String username;

    @NotEmpty
    @NotNull
    private String password;

    public RegisterRequest() {}

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

}
