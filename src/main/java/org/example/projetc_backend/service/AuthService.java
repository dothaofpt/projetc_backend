package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.LoginRequest;
import org.example.projetc_backend.dto.LoginResponse;
import org.example.projetc_backend.dto.RegisterRequest;
import org.example.projetc_backend.dto.UserResponse;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserService userService;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.allow-admin-registration:false}")
    private boolean allowAdminRegistration;

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService,
                       UserRepository userRepository, EmailService emailService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRepository = userRepository;
        this.emailService = emailService;
        this.passwordEncoder = passwordEncoder;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.username() == null || request.password() == null) {
            throw new IllegalArgumentException("Tên đăng nhập và mật khẩu là bắt buộc");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + request.username()));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token);
    }

    public LoginResponse register(RegisterRequest request) {
        if (request == null || request.username() == null || request.email() == null || request.password() == null) {
            throw new IllegalArgumentException("Tất cả các trường đăng ký là bắt buộc");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.email());
        }

        String role = request.role();
        if (!"ROLE_USER".equals(role) && (!"ROLE_ADMIN".equals(role) || !allowAdminRegistration)) {
            throw new IllegalArgumentException("Vai trò không hợp lệ hoặc không được phép gán ROLE_ADMIN");
        }

        UserResponse userResponse = userService.createUser(
                request.username(),
                request.email(),
                request.password(),
                request.fullName(),
                null, // avatarUrl là tùy chọn, mặc định null
                request.role()
        );

        String token = jwtUtil.generateToken(userResponse.username(), userResponse.role());
        return new LoginResponse(token);
    }

    public boolean userExists(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống");
        }
        return userRepository.existsByUsername(username);
    }

    public void sendOtpForPasswordReset(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email không được để trống");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại: " + email));

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        if (email == null || otp == null || newPassword == null) {
            throw new IllegalArgumentException("Email, OTP và mật khẩu mới là bắt buộc");
        }

        User user = userRepository.findByEmailAndOtpCode(email, otp)
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc OTP không hợp lệ"));

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    private String generateOtp() {
        Random random = new Random();
        return String.format("%06d", random.nextInt(1000000));
    }
}