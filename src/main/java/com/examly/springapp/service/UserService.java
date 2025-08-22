package com.examly.springapp.service;

import com.examly.springapp.dto.AuthResponse;
import com.examly.springapp.dto.LoginRequest;
import com.examly.springapp.dto.RegisterRequest;
import com.examly.springapp.entity.User;
import com.examly.springapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private WalletService walletService;
    
    @Transactional
    public AuthResponse registerUser(RegisterRequest request) {
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            return new AuthResponse("Email already exists", false, null, null);
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            return new AuthResponse("Username already exists", false, null, null);
        }
        
        // Create new user
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        
        User savedUser = userRepository.save(user);
        
        // Automatically create wallet for the new user
        try {
            walletService.createWallet(savedUser.getId());
        } catch (Exception e) {
            // Log the error but don't fail the registration
            // The wallet can be created later if needed
            System.err.println("Failed to create wallet for user " + savedUser.getId() + ": " + e.getMessage());
        }
        
        // Create response
        AuthResponse.UserData userData = new AuthResponse.UserData(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getEmail(),
            savedUser.getFullName()
        );
        
        return new AuthResponse("User registered successfully", true, null, userData);
    }
    
    public AuthResponse loginUser(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());
        
        if (userOptional.isEmpty()) {
            return new AuthResponse("Invalid email or password", false, null, null);
        }
        
        User user = userOptional.get();
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return new AuthResponse("Invalid email or password", false, null, null);
        }
        
        // Create response
        AuthResponse.UserData userData = new AuthResponse.UserData(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getFullName()
        );
        
        // In a real application, you would generate a JWT token here
        String token = "jwt-token-placeholder";
        
        return new AuthResponse("Login successful", true, token, userData);
    }
    
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
    
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
