package com.example.gestiontaches.repository;

import com.example.gestiontaches.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);
    List<Task> findByUserIdAndTitleContainingOrDescriptionContaining(
        Long userId, String title, String description);
}