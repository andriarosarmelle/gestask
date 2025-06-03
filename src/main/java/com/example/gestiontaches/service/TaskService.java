package com.example.gestiontaches.service;

import com.example.gestiontaches.model.Task;
import com.example.gestiontaches.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.PrintWriter;
import java.util.List;

@Service
public class TaskService {
    @Autowired
    private TaskRepository taskRepository;

    public List<Task> findTasksByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> findTasksByUserIdSorted(Long userId, String sort) {
        if ("date".equals(sort)) {
            return taskRepository.findByUserIdOrderByDueDateAsc(userId);
        } else if ("priority".equals(sort)) {
            return taskRepository.findByUserIdOrderByPriorityAsc(userId);
        }
        return taskRepository.findByUserId(userId);
    }

    public List<Task> searchTasks(Long userId, String keyword) {
        return taskRepository.searchByTitleAndUserId(userId, keyword);
    }

    public Task findTaskById(Long id) {
        return taskRepository.findById(id).orElse(null);
    }

    public void saveTask(Task task) {
        taskRepository.save(task);
    }

    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }

    public void exportTasksToCsv(Long userId, PrintWriter writer) {
        List<Task> tasks = taskRepository.findByUserId(userId);
        writer.write("ID,Title,Description,Due Date,Priority,Completed,Category\n");
        for (Task task : tasks) {
            String dueDate = task.getDueDate() != null ? task.getDueDate().toString() : "";
            String category = task.getCategory() != null ? task.getCategory().getName() : "";
            writer.write(String.format("%d,%s,%s,%s,%s,%s,%s\n",
                    task.getId(),
                    task.getTitle(),
                    task.getDescription(),
                    dueDate,
                    task.getPriority(),
                    task.isCompleted(),
                    category));
        }
    }
}