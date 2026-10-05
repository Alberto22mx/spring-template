package com.alberto.springtemplate.controller;

import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/** A minimal protected endpoint consumed by the Angular landing page. */
@RestController
@RequestMapping("/api/landing")
public class LandingController {

    @GetMapping
    public Map<String, String> getLanding(Jwt jwt) {
        return Map.of(
                "message", "Sesión autenticada correctamente",
                "email", jwt.getSubject()
        );
    }
}
