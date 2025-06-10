package com.example.gestiontaches.dto;

import java.time.LocalDateTime;

import com.example.gestiontaches.model.Category;
import com.example.gestiontaches.model.Task;

public class TaskDTO {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime dueDate;
    private boolean completed;
    private Task.Priority priority;
    private Category category;  
    private Long userId;

    // Getters
    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getDueDate() {
        return dueDate;
    }

    public boolean isCompleted() {
        return completed;
    }

    public Task.Priority getPriority() {
        return priority;
    }

    public Category getCategory() {
        return category;
    }

    public Long getUserId() {
        return userId;
    }

    // Setters
    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(LocalDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public void setPriority(Task.Priority priority) {
        this.priority = priority;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    // Méthodes de conversion
    public static TaskDTO fromTask(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setDueDate(task.getDueDate());
        dto.setCompleted(task.isCompleted());
        dto.setPriority(task.getPriority());
        dto.setCategory(task.getCategory());
        if (task.getUser() != null) {
            dto.setUserId(task.getUser().getId());
        }
        return dto;
    }

    public Task toTask() {
        Task task = new Task();
        task.setId(this.id);
        task.setTitle(this.title);
        task.setDescription(this.description);
        task.setDueDate(this.dueDate);
        task.setCompleted(this.completed);
        task.setPriority(this.priority);
        task.setCategory(this.category);
        return task;
    }
}