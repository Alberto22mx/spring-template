package com.alberto.springtemplate.dto.response;

public record AuthResponse(String token, String type) {

    public AuthResponse(String token) {
        this(token, "Bearer");
    }
}
