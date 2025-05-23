package org.example.projetc_backend.service;

import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.entity.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public User registerUser(String username, String email, String password, String fullName) {
        try {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setFullName(fullName);
            return userRepository.save(user);
        } catch (Exception e) {
            System.out.println("Lỗi khi lưu người dùng: " + e.getMessage());
            throw e;
        }
    }

    // Thêm getter để truy cập passwordEncoder
    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}