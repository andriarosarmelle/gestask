package com.example.gestiontaches.controller;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

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
    public String registerUser(User user) {
        userService.saveUser(user);
        return "redirect:/login";
    }

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentification) {
        model.addAttribute("user", userService.findByUsername(authentification.getName()));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfilePicture(@RequestParam("file") MultipartFile file, Authentication auth) throws Exception {
        User user = userService.findByUsername(auth.getName());
        userService.updateProfilePicture(user, file);
        return "redirect:/profile";
    }
}