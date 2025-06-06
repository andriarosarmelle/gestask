package com.example.gestiontaches.controller;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private static final String UPLOAD_DIR = "uploads/profiles/";

    private final UserService userService;

    @Autowired
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String login(Model model, 
                       @RequestParam(required = false) String error,
                       @RequestParam(required = false) String registered) {
        if (error != null) {
            model.addAttribute("error", "Nom d'utilisateur ou mot de passe incorrect");
        }
        if (registered != null) {
            model.addAttribute("message", "Inscription réussie ! Vous pouvez maintenant vous connecter.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        if (!model.containsAttribute("user")) {
            model.addAttribute("user", new User());
        }
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user, 
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        logger.debug("Tentative d'inscription pour l'utilisateur: {}", user.getUsername());

        if (result.hasErrors()) {
            return "register";
        }

        try {
            userService.saveUser(user);
            logger.info("Inscription réussie pour l'utilisateur: {}", user.getUsername());
            redirectAttributes.addFlashAttribute("registered", true);
            return "redirect:/login?registered";
        } catch (Exception e) {
            logger.error("Erreur lors de l'inscription: {}", e.getMessage());
            result.rejectValue("username", "error.user", e.getMessage());
            return "register";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(auth.getName());
        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String password,
                              @RequestParam(required = false) String email,
                              RedirectAttributes redirectAttributes) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        try {
            User user = userService.findByUsername(auth.getName());
            userService.updateProfile(user.getId(), password, email);
            redirectAttributes.addFlashAttribute("success", "Profil mis à jour avec succès");
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour du profil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        
        return "redirect:/profile";
    }

    @PostMapping("/profile/picture")
    public String updateProfilePicture(@RequestParam("file") MultipartFile file,
                                     RedirectAttributes redirectAttributes) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return "redirect:/login";
            }

            User currentUser = userService.findByUsername(auth.getName());
            if (currentUser == null) {
                return "redirect:/login";
            }

            if (file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Veuillez sélectionner un fichier");
                return "redirect:/profile";
            }

            // Vérification du type de fichier
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                redirectAttributes.addFlashAttribute("error", "Seules les images sont autorisées");
                return "redirect:/profile";
            }

            // Création du répertoire si nécessaire
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists() && !uploadDir.mkdirs()) {
                throw new IOException("Impossible de créer le répertoire de stockage");
            }

            // Sauvegarde du fichier
            String fileName = String.format("%d_%d_%s", 
                currentUser.getId(), 
                System.currentTimeMillis(),
                file.getOriginalFilename());
            
            Path path = Paths.get(UPLOAD_DIR + fileName);
            Files.write(path, file.getBytes());

            userService.updateProfilePicture(currentUser.getId(), UPLOAD_DIR + fileName);
            redirectAttributes.addFlashAttribute("success", "Photo de profil mise à jour avec succès");

        } catch (IOException e) {
            logger.error("Erreur lors de la mise à jour de la photo de profil: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Erreur lors du téléchargement de l'image");
        }
        
        return "redirect:/profile";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "access-denied";
    }
}