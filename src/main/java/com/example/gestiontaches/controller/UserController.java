package com.example.gestiontaches.controller;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    private static final String UPLOAD_DIR = "uploads/profiles/";

    @PostMapping("/profile/picture")
    public String updateProfilePicture(@RequestParam("file") MultipartFile file) {
        try {
            // Récupérer l'utilisateur connecté
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            User currentUser = userService.findByUsername(auth.getName());

            if (currentUser != null) {
                // Créer le répertoire de stockage s'il n'existe pas
                File uploadDir = new File(UPLOAD_DIR);
                if (!uploadDir.exists()) {
                    uploadDir.mkdirs();
                }

                // Générer un nom de fichier unique
                String fileName = currentUser.getId() + "_" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path path = Paths.get(UPLOAD_DIR + fileName);

                // Sauvegarder le fichier
                Files.write(path, file.getBytes());

                // Mettre à jour le chemin dans la base de données
                userService.updateProfilePicture(currentUser.getId(), UPLOAD_DIR + fileName);

                return "redirect:/user/profile?success";
            }
        } catch (IOException e) {
            // Log l'erreur ici si nécessaire
        }
        return "redirect:/user/profile?error";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User user = userService.findByUsername(auth.getName());
            if (user != null) {
                model.addAttribute("user", user);
                return "profile";
            }
        }
        return "redirect:/login";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam(required = false) String password,
                              @RequestParam(required = false) String email) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            User user = userService.findByUsername(auth.getName());
            if (user != null) {
                userService.updateProfile(user.getId(), password, email);
                return "redirect:/user/profile?updated";
            }
        }
        return "redirect:/login";
    }
}