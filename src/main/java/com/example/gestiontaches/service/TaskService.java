package com.example.gestiontaches.service;

import com.example.gestiontaches.dto.TaskDTO;
import com.example.gestiontaches.model.Task;
import com.example.gestiontaches.model.Category;
import com.example.gestiontaches.repository.TaskRepository;
import com.example.gestiontaches.repository.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.PrintWriter;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final DateTimeFormatter dateFormatter;

    @Autowired
    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository) {
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> findTasksByUserId(Long userId) {
        return taskRepository.findByUserIdWithCategories(userId)
            .stream()
            .map(TaskDTO::fromTask)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> findTasksByUserIdSorted(Long userId, String sort) {
        List<TaskDTO> tasks = taskRepository.findByUserIdWithCategories(userId)
            .stream()
            .map(TaskDTO::fromTask)
            .toList();
        
        return switch (sort.toLowerCase()) {
            case "priority" -> tasks.stream()
                .sorted(Comparator.comparing(TaskDTO::getPriority))
                .toList();
            case "date" -> tasks.stream()
                .sorted(Comparator.comparing(TaskDTO::getDueDate, 
                    Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
            default -> tasks;
        };
    }

    @Transactional
    public Task saveTask(Task task, Long categoryId) {
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + categoryId));
            task.setCategory(category);
        } else {
            task.setCategory(null);
        }
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public TaskDTO findTaskById(Long id) {
        Task task = taskRepository.findByIdWithCategory(id)
            .orElseThrow(() -> new RuntimeException("Task not found with id: " + id));
        return TaskDTO.fromTask(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<TaskDTO> searchTasks(Long userId, String keyword) {
        return taskRepository.findByUserIdAndKeyword(userId, keyword)
            .stream()
            .map(TaskDTO::fromTask)
            .toList();
    }

    @Transactional(readOnly = true)
    public void exportTasksToCsv(Long userId, PrintWriter writer) {
        List<TaskDTO> tasks = taskRepository.findByUserIdWithCategories(userId)
            .stream()
            .map(TaskDTO::fromTask)
            .toList();
        
        writer.println("Titre,Description,Date d'échéance,Priorité,Statut,Catégorie");
        
        tasks.forEach(task -> {
            String[] fields = {
                escapeCsv(task.getTitle()),
                escapeCsv(task.getDescription()),
                task.getDueDate() != null ? task.getDueDate().format(dateFormatter) : "",
                task.getPriority().toString(),
                task.isCompleted() ? "Terminée" : "En cours",
                task.getCategory() != null ? escapeCsv(task.getCategory().getName()) : ""
            };
            writer.println(String.join(",", fields));
        });
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }
}