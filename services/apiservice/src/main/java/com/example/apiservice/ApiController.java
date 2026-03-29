package com.example.apiservice;

import com.example.apiservice.kafka.UserProducer;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    private final UserProducer userProducer;

    @Value("${AUTH_SERVICE_URL}")
    private String authServiceUrl;

    @Value("${USER_SERVICE_URL}")
    private String userServiceUrl;

    public ApiController(UserProducer userProducer) {
        this.userProducer = userProducer;
    }

    // ---------- SIGNUP (NOW KAFKA BASED) ----------
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody User user) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String userJson = mapper.writeValueAsString(user);

            userProducer.sendUserEvent(userJson);

            return ResponseEntity.ok("User event sent to Kafka");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Signup failed");
        }
    }

    // ---------- LOGIN (UNCHANGED REST FLOW) ----------
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody Map<String, String> body) {

        try {
            String token = rest.postForObject(
                    authServiceUrl + "/login",
                    body,
                    String.class
            );

            return ResponseEntity.ok(token);

        } catch (HttpClientErrorException | HttpServerErrorException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid credentials");
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Login failed");
        }
    }
}
