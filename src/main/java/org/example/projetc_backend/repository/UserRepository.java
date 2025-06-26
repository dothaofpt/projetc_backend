package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.User;
import org.springframework.data.domain.Page; // Don't forget to import Page
import org.springframework.data.domain.Pageable; // Don't forget to import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Don't forget to import Param

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndOtpCode(String email, String otpCode);
    List<User> findByRole(User.Role role);

    @Query("SELECT u FROM User u WHERE u.username LIKE %:keyword% OR u.email LIKE %:keyword%")
    List<User> findByUsernameOrEmailContaining(String keyword);

    // --- Add this method for searching and pagination ---
    @Query("SELECT u FROM User u WHERE " +
            "(:username IS NULL OR LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%'))) AND " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) AND " +
            "(:fullName IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :fullName, '%'))) AND " +
            "(:role IS NULL OR u.role = :role)")
    Page<User> searchUsers(
            @Param("username") String username,
            @Param("email") String email,
            @Param("fullName") String fullName,
            @Param("role") User.Role role,
            Pageable pageable
    );
}