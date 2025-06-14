package com.inventory.service;

import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    public List<User> getAllActiveUsers() {
        return userRepository.findByIsActiveTrue();
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    public User createUser(String username, String password, User.Role role) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role);
        user.setIsActive(true);
        
        return userRepository.save(user);
    }
    
    public User updateUser(Long id, String username, User.Role role) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        // Check if username is being changed and if it already exists
        if (!user.getUsername().equals(username) && userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        
        user.setUsername(username);
        user.setRole(role);
        
        return userRepository.save(user);
    }
    
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setIsActive(false);
        userRepository.save(user);
    }
    
    public void changePassword(Long id, String newPassword) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void initializeDefaultAdmin() {
        Optional<User> existingAdmin = userRepository.findByUsername("admin");
        if (existingAdmin.isPresent()) {
            // Update existing admin user to ensure isActive is set
            User admin = existingAdmin.get();
            if (admin.getIsActive() == null) {
                admin.setIsActive(true);
                userRepository.save(admin);
            }
        } else {
            // Create new admin user
            createUser("admin", "admin123", User.Role.ADMIN);
        }
    }
} 