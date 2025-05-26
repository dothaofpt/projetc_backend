package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LoginRequest;
import org.example.projetc_backend.dto.LoginResponse;
import org.example.projetc_backend.dto.RegisterRequest;
import org.example.projetc_backend.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@RequestBody RegisterRequest request) {
        LoginResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/check-user/{username}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable String username) {
        boolean exists = authService.userExists(username);
        return ResponseEntity.ok(exists);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendOtpForPasswordReset(@RequestParam String email) {
        authService.sendOtpForPasswordReset(email);
        return ResponseEntity.ok("OTP đã được gửi đến email của bạn");
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email,
                                                @RequestParam String otp,
                                                @RequestParam String newPassword) {
        authService.resetPassword(email, otp, newPassword);
        return ResponseEntity.ok("Mật khẩu đã được đặt lại thành công");
    }
}
