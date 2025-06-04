package com.example.gestiontaches.service;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional
    public User saveUser(User user) {
        // Encodage du mot de passe avant sauvegarde
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    @Transactional
    public void updateProfile(Long userId, String password, String email) {
        User user = userRepository.findById(userId).orElseThrow(() -> 
            new RuntimeException("Utilisateur non trouvé"));
        
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }
        if (email != null && !email.isEmpty()) {
            user.setEmail(email);
        }
        
        userRepository.save(user);
    }

    @Transactional
    public void updateProfilePicture(Long userId, String picturePath) {
        User user = userRepository.findById(userId).orElseThrow(() -> 
            new RuntimeException("Utilisateur non trouvé"));
        user.setProfilePicture(picturePath);
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public String getProfilePicture(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> 
            new RuntimeException("Utilisateur non trouvé"));
        return user.getProfilePicture();
    }

    // Méthode pour gérer les exceptions de manière plus spécifique
    private void handleException(Exception e) {
        if (e instanceof org.springframework.dao.DataIntegrityViolationException) {
            throw new RuntimeException("Erreur de contrainte d'intégrité", e);
        } else {
            throw new RuntimeException("Erreur lors de l'opération", e);
        }
    }
}