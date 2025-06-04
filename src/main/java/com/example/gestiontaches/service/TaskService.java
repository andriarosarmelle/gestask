package com.example.gestiontaches.service;

import com.example.gestiontaches.model.Task;
import com.example.gestiontaches.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Transactional(readOnly = true)
    public List<Task> findTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Task> findTasksByUserIdSorted(Long userId, String sort) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        
        switch (sort.toLowerCase()) {
            case "priority":
                return tasks.stream()
                    .sorted(Comparator.comparing(Task::getPriority))
                    .collect(Collectors.toList());
            case "due_date":
                return tasks.stream()
                    .sorted(Comparator.comparing(Task::getDueDate, 
                        Comparator.nullsLast(Comparator.naturalOrder())))
                    .collect(Collectors.toList());
            case "title":
                return tasks.stream()
                    .sorted(Comparator.comparing(Task::getTitle))
                    .collect(Collectors.toList());
            default:
                return tasks;
        }
    }

    @Transactional
    public Task saveTask(Task task) {
        return taskRepository.save(task);
    }

    @Transactional(readOnly = true)
    public Task findTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> searchTasks(Long userId, String keyword) {
        return taskRepository.findByUserIdAndTitleContainingOrDescriptionContaining(
            userId, keyword, keyword);
    }

    @Transactional(readOnly = true)
    public void exportTasksToCsv(Long userId, PrintWriter writer) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        List<Task> tasks = findTasksByUserId(userId);

        // En-tête CSV
        writer.println("Titre,Description,Date d'échéance,Priorité,Statut,Catégorie");

        // Contenu
        tasks.forEach(task -> {
            String[] fields = {
                escapeCsv(task.getTitle()),
                escapeCsv(task.getDescription()),
                task.getDueDate() != null ? dateFormat.format(task.getDueDate()) : "",
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