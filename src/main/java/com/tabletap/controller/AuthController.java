package com.tabletap.controller;
import com.tabletap.config.JwtService;
import com.tabletap.dto.request.LoginRequest;
import com.tabletap.dto.response.LoginResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController @RequestMapping("/api/auth") @RequiredArgsConstructor
public class AuthController {
    private final JwtService jwtService;
    @Value("${app.restaurant-username:admin}") private String username;
    @Value("${app.restaurant-password:admin123}") private String password;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        if (username.equals(req.username()) && password.equals(req.password())) {
            return ResponseEntity.ok(new LoginResponse(jwtService.generate(req.username()), req.username()));
        }
        return ResponseEntity.status(401).body("Invalid credentials");
    }
}