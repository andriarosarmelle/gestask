package com.example.gestiontaches.controller;

import com.example.gestiontaches.dto.TaskDTO;
import com.example.gestiontaches.model.Task;
import com.example.gestiontaches.service.CategoryService;
import com.example.gestiontaches.service.TaskService;
import com.example.gestiontaches.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static final Logger logger = LoggerFactory.getLogger(TaskController.class);

    private final TaskService taskService;
    private final CategoryService categoryService;
    private final UserService userService;

    @Autowired
    public TaskController(TaskService taskService, CategoryService categoryService, UserService userService) {
        this.taskService = taskService;
        this.categoryService = categoryService;
        this.userService = userService;
    }

    @GetMapping
    public String listTasks(Model model, Authentication auth,
                          @RequestParam(value = "sort", required = false) String sort,
                          @RequestParam(value = "search", required = false) String search) {
        try {
            Long userId = getUserId(auth);
            List<TaskDTO> tasks;

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
            model.addAttribute("sort", sort); // Ajout pour le template
            model.addAttribute("search", search); // Ajout pour le template
            return "tasks";
        } catch (Exception e) {
            logger.error("Error in listTasks: ", e);
            model.addAttribute("error", "Une erreur est survenue lors du chargement des tâches.");
            return "error";
        }
    }

    @GetMapping("/new")
    public String showTaskForm(Model model, Authentication auth) {
        try {
            Long userId = getUserId(auth);
            model.addAttribute("task", new TaskDTO());
            model.addAttribute("categories", categoryService.findCategoriesByUserId(userId));
            model.addAttribute("currentUser", userService.findByUsername(auth.getName()));
            model.addAttribute("priorities", Task.Priority.values()); // Ajout des priorités
            return "edit-task";
        } catch (Exception e) {
            logger.error("Error in showTaskForm: ", e);
            model.addAttribute("error", "Une erreur est survenue lors de l'affichage du formulaire.");
            return "error";
        }
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, Authentication auth) {
        try {
            Long userId = getUserId(auth);
            TaskDTO task = taskService.findTaskById(id);
            
            if (!task.getUserId().equals(userId)) {
                return "redirect:/tasks";
            }
            
            model.addAttribute("task", task);
            model.addAttribute("categories", categoryService.findCategoriesByUserId(userId));
            model.addAttribute("currentUser", userService.findByUsername(auth.getName()));
            model.addAttribute("priorities", Task.Priority.values()); // Ajout des priorités
            return "edit-task";
        } catch (Exception e) {
            logger.error("Error in showEditForm: ", e);
            model.addAttribute("error", "Une erreur est survenue lors de l'édition de la tâche.");
            return "error";
        }
    }

    @PostMapping
    public String saveTask(@ModelAttribute TaskDTO taskDTO,
                         @RequestParam(required = false) Long categoryId,
                         Authentication auth) {
        try {
            Task task = taskDTO.toTask();
            task.setUser(userService.findByUsername(auth.getName()));
            taskService.saveTask(task, categoryId);
            return "redirect:/tasks";
        } catch (Exception e) {
            logger.error("Error in saveTask: ", e);
            return "redirect:/tasks?error=save";
        }
    }

    @PostMapping("/delete/{id}")
    public String deleteTask(@PathVariable Long id, Authentication auth) {
        try {
            TaskDTO task = taskService.findTaskById(id);
            if (task.getUserId().equals(getUserId(auth))) {
                taskService.deleteTask(id);
            }
            return "redirect:/tasks";
        } catch (Exception e) {
            logger.error("Error in deleteTask: ", e);
            return "redirect:/tasks?error=delete";
        }
    }

    @GetMapping("/export")
    public void exportTasks(HttpServletResponse response, Authentication auth) throws IOException {
        try {
            response.setContentType("text/csv");
            response.setHeader("Content-Disposition", "attachment; filename=tasks.csv");
            taskService.exportTasksToCsv(getUserId(auth), response.getWriter());
        } catch (IOException e) {
            logger.error("Error in exportTasks: ", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erreur lors de l'exportation");
        }
    }

    private Long getUserId(Authentication auth) {
        return userService.findByUsername(auth.getName()).getId();
    }

    @ExceptionHandler(Exception.class)
    public String handleError(Exception e, Model model) {
        logger.error("Unexpected error: ", e);
        model.addAttribute("error", "Une erreur inattendue s'est produite.");
        return "error";
    }
}