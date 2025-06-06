package com.example.gestiontaches.service;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        try {
            if (username == null || username.trim().isEmpty()) {
                logger.error("Tentative de recherche avec un nom d'utilisateur vide");
                throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas être vide");
            }
            return userRepository.findByUsername(username);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation: {}", e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erreur d'accès à la base de données pour username {}: {}", username, e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche de l'utilisateur", e);
        }
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        try {
            if (email == null || email.trim().isEmpty()) {
                logger.error("Tentative de recherche avec un email vide");
                throw new IllegalArgumentException("L'email ne peut pas être vide");
            }
            return userRepository.findByEmail(email);
        } catch (IllegalArgumentException e) {
            logger.error("Erreur de validation email: {}", e.getMessage());
            throw e;
        } catch (DataIntegrityViolationException e) {
            logger.error("Erreur d'accès à la base de données pour email {}: {}", email, e.getMessage());
            throw new RuntimeException("Erreur lors de la recherche par email", e);
        }
    }

    @Transactional
    public User saveUser(User user) {
        try {
            validateNewUser(user);
            User existingUsername = userRepository.findByUsername(user.getUsername());
            if (existingUsername != null) {
                logger.warn("Tentative de création avec un nom d'utilisateur existant: {}", user.getUsername());
                throw new IllegalArgumentException("Ce nom d'utilisateur existe déjà");
            }

            User existingEmail = userRepository.findByEmail(user.getEmail());
            if (existingEmail != null) {
                logger.warn("Tentative de création avec un email existant: {}", user.getEmail());
                throw new IllegalArgumentException("Cet email existe déjà");
            }

            user.setPassword(passwordEncoder.encode(user.getPassword()));
            User savedUser = userRepository.save(user);
            logger.info("Nouvel utilisateur créé avec succès: {}", user.getUsername());
            return savedUser;

        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            logger.error("Erreur lors de la création de l'utilisateur: {}", e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void updateProfile(Long userId, String password, String email) {
        try {
            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

            if (email != null && !email.trim().isEmpty() && !email.equals(user.getEmail())) {
                User existingEmail = userRepository.findByEmail(email);
                if (existingEmail != null && !existingEmail.getId().equals(userId)) {
                    throw new IllegalArgumentException("Cet email est déjà utilisé");
                }
                user.setEmail(email);
            }

            if (password != null && !password.trim().isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            userRepository.save(user);
            logger.info("Profil mis à jour avec succès pour l'utilisateur ID: {}", userId);

        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            logger.error("Erreur lors de la mise à jour du profil pour l'ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    @Transactional
    public void updateProfilePicture(Long userId, String picturePath) {
        try {
            if (picturePath == null || picturePath.trim().isEmpty()) {
                throw new IllegalArgumentException("Le chemin de l'image ne peut pas être vide");
            }

            User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId));

            user.setProfilePicture(picturePath);
            userRepository.save(user);
            logger.info("Photo de profil mise à jour pour l'utilisateur ID: {}", userId);

        } catch (IllegalArgumentException | DataIntegrityViolationException e) {
            logger.error("Erreur lors de la mise à jour de la photo de profil pour l'ID {}: {}", userId, e.getMessage());
            throw e;
        }
    }

    private void validateNewUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null");
        }
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Le nom d'utilisateur est obligatoire");
        }
        if (user.getPassword() == null || user.getPassword().length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
        }
        if (user.getEmail() == null || !user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new IllegalArgumentException("Format d'email invalide");
        }
    }
}