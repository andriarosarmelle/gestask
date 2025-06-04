package com.example.gestiontaches.service;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public User saveUser(User user) {
        // Vérifier si l'utilisateur existe déjà
        if (userRepository.findByUsername(user.getUsername()) != null) {
            throw new RuntimeException("Le nom d'utilisateur existe déjà");
        }
        
        // Encoder le mot de passe avant de sauvegarder
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public User updateProfilePicture(User user, MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = Paths.get("src/main/resources/static/uploads/" + fileName);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, file.getBytes());
            user.setProfilePicture("/uploads/" + fileName);
        }
        return userRepository.save(user);
    }
}