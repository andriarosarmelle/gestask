package com.example.gestiontaches.controller;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.service.UserService;
import jakarta.validation.Valid;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user, 
                             BindingResult result, 
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "register";
        }

        try {
            if (userService.findByUsername(user.getUsername()) != null) {
                result.rejectValue("username", "error.user", "Ce nom d'utilisateur existe déjà");
                return "register";
            }
            userService.saveUser(user);
            redirectAttributes.addFlashAttribute("success", "Inscription réussie !");
            return "redirect:/login";
        } catch (Exception e) {
            result.rejectValue("global", "error.global", "Une erreur est survenue lors de l'inscription");
            return "register";
        }
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@RequestParam("file") MultipartFile file, 
                              Authentication authentication) throws IOException {
        User user = userService.findByUsername(authentication.getName());
        userService.updateProfilePicture(user, file);
        return "redirect:/profile";
    }
}