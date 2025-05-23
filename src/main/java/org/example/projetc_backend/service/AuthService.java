package org.example.projetc_backend.service;

import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.dto.LoginRequest;
import org.example.projetc_backend.dto.LoginResponse;
import org.example.projetc_backend.dto.RegisterRequest;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.security.JwtUtil;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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

    public AuthService(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserService userService,
                       UserRepository userRepository, EmailService emailService) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userService = userService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    public LoginResponse login(LoginRequest request) throws AuthenticationException {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        String token = jwtUtil.generateToken(request.username());
        return new LoginResponse(token);
    }

    public LoginResponse register(RegisterRequest request) {
        if (userService.findByUsername(request.username()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        if (userService.findByEmail(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email đã tồn tại");
        }

        userService.registerUser(
                request.username(),
                request.email(),
                request.password(),
                request.fullName()
        );

        String token = jwtUtil.generateToken(request.username());
        return new LoginResponse(token);
    }

    public boolean userExists(String username) {
        return userService.findByUsername(username).isPresent();
    }

    public void sendOtpForPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        String otp = generateOtp();
        user.setOtpCode(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));
        userRepository.save(user);

        emailService.sendOtpEmail(email, otp);
    }

    public void resetPassword(String email, String otp, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Email không tồn tại"));

        if (user.getOtpCode() == null || !user.getOtpCode().equals(otp) ||
                user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Mã OTP không hợp lệ hoặc đã hết hạn");
        }

        // Sử dụng getter thay vì truy cập trực tiếp
        user.setPassword(userService.getPasswordEncoder().encode(newPassword));
        user.setOtpCode(null);
        user.setOtpExpiry(null);
        userRepository.save(user);
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }
}