package com.example.gestiontaches.controller;

import com.example.gestiontaches.model.Category;
import com.example.gestiontaches.model.User;
import com.example.gestiontaches.service.CategoryService;
import com.example.gestiontaches.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listCategories(Model model, Authentication auth) {
        Long userId = getUserId(auth);
        model.addAttribute("categories", categoryService.findCategoriesByUserId(userId));
        model.addAttribute("category", new Category());
        model.addAttribute("currentUser", userService.findByUsername(auth.getName()));
        return "categories";
    }

    @PostMapping
    public String saveCategory(@ModelAttribute Category category, Authentication auth) {
        category.setUser(userService.findByUsername(auth.getName()));
        categoryService.saveCategory(category);
        return "redirect:/categories";
    }

    @PostMapping("/delete/{id}")
    public String deleteCategory(@PathVariable Long id, Authentication auth) {
        Long userId = getUserId(auth);
        Category category = categoryService.findCategoriesByUserId(userId)
                .stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElse(null);
        if (category != null) {
            categoryService.deleteCategory(id);
        }
        return "redirect:/categories";
    }

    private Long getUserId(Authentication auth) {
        String username = auth.getName();
        User user = userService.findByUsername(username);
        if (user == null) {
            throw new IllegalStateException("Utilisateur non trouvé : " + username);
        }
        return user.getId();
    }
}