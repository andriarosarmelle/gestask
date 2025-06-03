package com.example.gestiontaches.service;

import com.example.gestiontaches.model.Category;
import com.example.gestiontaches.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    public List<Category> findCategoriesByUserId(Long userId) {
        return categoryRepository.findByUserId(userId);
    }

    public void saveCategory(Category category) {
        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}