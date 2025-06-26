package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LoginRequest;
import org.example.projetc_backend.dto.LoginResponse;
import org.example.projetc_backend.dto.RegisterRequest;
import org.example.projetc_backend.service.AuthService;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Import Valid

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * Endpoint để người dùng đăng nhập.
     * @param request DTO chứa tên đăng nhập và mật khẩu.
     * @return ResponseEntity với LoginResponse chứa JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) { // Bắt các ngoại lệ chung cho lỗi xác thực (ví dụ: BadCredentialsException)
            return new ResponseEntity<>(new LoginResponse("Xác thực thất bại: " + e.getMessage()), HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Endpoint để người dùng đăng ký tài khoản mới.
     * @param request DTO chứa thông tin đăng ký (username, email, password, fullName, role).
     * @return ResponseEntity với LoginResponse chứa JWT token (nếu đăng ký thành công và tự động đăng nhập).
     */
    @PostMapping("/register")
    public ResponseEntity<LoginResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            LoginResponse response = authService.register(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Kiểm tra xem tên người dùng có tồn tại hay không.
     * @param username Tên người dùng cần kiểm tra.
     * @return ResponseEntity với boolean (true nếu tồn tại, false nếu không).
     */
    @GetMapping("/check-user/{username}")
    public ResponseEntity<Boolean> checkUserExists(@PathVariable String username) {
        try {
            boolean exists = authService.userExists(username);
            return new ResponseEntity<>(exists, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST); // Nếu username rỗng/null
        }
    }

    /**
     * Gửi mã OTP đến email để đặt lại mật khẩu.
     * @param email Địa chỉ email của người dùng.
     * @return ResponseEntity với thông báo thành công.
     */
    @PostMapping("/forgot-password")
    public ResponseEntity<String> sendOtpForPasswordReset(@RequestParam String email) {
        try {
            authService.sendOtpForPasswordReset(email);
            return new ResponseEntity<>("Mã OTP đã được gửi đến email của bạn.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Đặt lại mật khẩu bằng mã OTP.
     * @param email Địa chỉ email của người dùng.
     * @param otp Mã OTP đã nhận được.
     * @param newPassword Mật khẩu mới.
     * @return ResponseEntity với thông báo thành công.
     */
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestParam String email,
                                                @RequestParam String otp,
                                                @RequestParam String newPassword) {
        try {
            authService.resetPassword(email, otp, newPassword);
            return new ResponseEntity<>("Mật khẩu đã được đặt lại thành công.", HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }
}