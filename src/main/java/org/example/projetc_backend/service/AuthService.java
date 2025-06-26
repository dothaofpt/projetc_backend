// src/main/java/org/example/projetc_backend/service/AuthService.java
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
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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
            throw new IllegalArgumentException("Tên đăng nhập và mật khẩu là bắt buộc.");
        }

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new BadCredentialsException("Tên đăng nhập hoặc mật khẩu không đúng.");
        } catch (AuthenticationException e) {
            throw new RuntimeException("Lỗi xác thực: " + e.getMessage());
        }

        User user = userRepository.findByUsername(request.username())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng: " + request.username()));

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole().name());
        return new LoginResponse(token);
    }

    /**
     * Xử lý yêu cầu đăng ký người dùng mới.
     * Logic này sẽ được sử dụng cho cả đăng ký thông thường và đăng ký admin (nếu được phép).
     * @param request DTO chứa thông tin đăng ký (bao gồm cả vai trò).
     * @return LoginResponse chứa JWT token.
     */
    public LoginResponse register(RegisterRequest request) {
        if (request == null || request.username() == null || request.email() == null || request.password() == null) {
            throw new IllegalArgumentException("Tất cả các trường đăng ký (tên đăng nhập, email, mật khẩu) là bắt buộc.");
        }

        if (userRepository.existsByUsername(request.username())) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại: " + request.username());
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new IllegalArgumentException("Email đã tồn tại: " + request.email());
        }

        // Xác định vai trò sẽ gán
        User.Role requestedRole = request.role();
        String roleToAssign;

        if (requestedRole == null || requestedRole == User.Role.ROLE_USER) {
            // Mặc định là ROLE_USER nếu không được chỉ định hoặc được chỉ định là USER
            roleToAssign = User.Role.ROLE_USER.name();
        } else if (requestedRole == User.Role.ROLE_ADMIN) {
            // Nếu vai trò yêu cầu là ADMIN, kiểm tra cờ allowAdminRegistration
            if (!allowAdminRegistration) {
                throw new IllegalArgumentException("Không được phép đăng ký vai trò ADMIN qua endpoint này.");
            }
            roleToAssign = User.Role.ROLE_ADMIN.name();
        } else {
            // Xử lý các vai trò không hợp lệ khác (nếu có)
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + requestedRole.name());
        }

        String fullName = request.fullName() != null ? request.fullName().trim() : null;

        UserResponse userResponse = userService.createUser(
                request.username().trim(),
                request.email().trim(),
                request.password().trim(),
                fullName,
                null, // avatarUrl là tùy chọn, mặc định null
                roleToAssign // Truyền String role
        );

        String token = jwtUtil.generateToken(userResponse.username(), userResponse.role().name());
        return new LoginResponse(token);
    }

    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        }
        return userRepository.existsByUsername(username);
    }

    public void sendOtpForPasswordReset(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
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
        if (email == null || email.trim().isEmpty() || otp == null || otp.trim().isEmpty() || newPassword == null || newPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Email, OTP và mật khẩu mới là bắt buộc.");
        }

        User user = userRepository.findByEmailAndOtpCode(email, otp)
                .orElseThrow(() -> new IllegalArgumentException("Email hoặc OTP không hợp lệ."));

        if (user.getOtpExpiry() == null || user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP đã hết hạn.");
        }

        if (newPassword.length() < 6) {
            throw new IllegalArgumentException("Mật khẩu mới phải có ít nhất 6 ký tự.");
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