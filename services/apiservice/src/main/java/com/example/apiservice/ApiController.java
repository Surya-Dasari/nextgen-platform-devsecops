package com.example.apiservice;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@RestController
@CrossOrigin
public class ApiController {

    private final RestTemplate rest = new RestTemplate();

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Value("${USER_SERVICE_URL}")
    private String userServiceUrl;

    // ---------- SIGNUP ----------
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        try {
            String response = rest.postForObject(
                    userServiceUrl + "/users",
                    user,
                    String.class
            );
            return ResponseEntity.ok(response);

	  } catch (Exception e) {
    e.printStackTrace();
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body("Signup failed: " + e.getMessage());
}
    }

    // ---------- LOGIN (JWT PASS-THROUGH) ----------
    @PostMapping("/login")
public ResponseEntity<String> login(@RequestBody User user) {

    try {
        String token = rest.postForObject(
                authServiceUrl + "/login",
                user,
                String.class
        );

        return ResponseEntity.ok(token);

    } catch (HttpClientErrorException | HttpServerErrorException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid credentials");
    } catch (Exception ex) {
        ex.printStackTrace();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Login failed: " + ex.getMessage());
    }
}
}
