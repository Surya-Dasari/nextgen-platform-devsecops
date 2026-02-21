package com.example.authservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@CrossOrigin
public class AuthController {

    @Value("${userservice.url}")
    private String userServiceUrl;

    private final RestTemplate rest = new RestTemplate();

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody User user) {

        Boolean ok = rest.postForObject(
                userServiceUrl + "/users/validate",
                user,
                Boolean.class
        );

        if (Boolean.TRUE.equals(ok)) {
            String token = JwtUtil.generateToken(user.getUsername());
            return ResponseEntity.ok(token);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        }
    }
}

