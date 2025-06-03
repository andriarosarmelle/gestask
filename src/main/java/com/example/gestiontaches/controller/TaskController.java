package com.example.gestiontaches.controller;

import com.example.gestiontaches.model.Task;
import com.example.gestiontaches.model.User;
import com.example.gestiontaches.service.CategoryService;
import com.example.gestiontaches.service.TaskService;
import com.example.gestiontaches.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private UserService userService;

    @GetMapping
    public String listTasks(Model model, Authentication auth,
                            @RequestParam(value = "sort", required = false) String sort,
                            @RequestParam(value = "search", required = false) String search) {
        Long userId = getUserId(auth);
        List<Task> tasks;

        if (search != null && !search.isEmpty()) {
            tasks = taskService.searchTasks(userId, search);
        } else {
            tasks = (sort != null && !sort.isEmpty()) ?
                    taskService.findTasksByUserIdSorted(userId, sort) :
                    taskService.findTasksByUserId(userId);
        }

        model.addAttribute("tasks", tasks);
        model.addAttribute("categories", categoryService.findCategoriesByUserId(userId));
        model.addAttribute("currentUser", userService.findByUsername(auth.getName()));
        return "tasks";
    }

    @GetMapping("/new")
    public String showTaskForm(Model model, Authentication auth) {
        model.addAttribute("task", new Task());
        model.addAttribute("categories", categoryService.findCategoriesByUserId(getUserId(auth)));
        return "edit-task";
    }

    @PostMapping
    public String saveTask(@ModelAttribute Task task, Authentication auth) {
        task.setUser(userService.findByUsername(auth.getName()));
        taskService.saveTask(task);
        return "redirect:/tasks";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication auth) {
        Task task = taskService.findTaskById(id);
        if (task.getUser().getId().equals(getUserId(auth))) {
            model.addAttribute("task", task);
            model.addAttribute("categories", categoryService.findCategoriesByUserId(getUserId(auth)));
            return "edit-task";
        }
        return "redirect:/tasks";
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, Authentication auth) {
        Task task = taskService.findTaskById(id);
        if (task.getUser().getId().equals(getUserId(auth))) {
            taskService.deleteTask(id);
        }
        return "redirect:/tasks";
    }

    @GetMapping("/export")
    public void exportTasks(HttpServletResponse response, Authentication auth) throws IOException {
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=tasks.csv");
        taskService.exportTasksToCsv(getUserId(auth), response.getWriter());
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