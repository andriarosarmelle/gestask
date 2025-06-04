package com.example.gestiontaches.controller;

import com.example.gestiontaches.model.User;
import com.example.gestiontaches.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public String showProfile(Model model, Authentication authentication) {
        User user = userService.findByUsername(authentication.getName());
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/update")
    public String updateProfile(@RequestParam("profilePicture") MultipartFile file, 
                              Authentication authentication) throws IOException {
        User user = userService.findByUsername(authentication.getName());
        userService.updateProfilePicture(user, file);
        return "redirect:/user/profile";
    }
}