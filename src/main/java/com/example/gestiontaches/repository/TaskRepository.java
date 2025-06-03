package com.example.gestiontaches.repository;

import com.example.gestiontaches.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByUserId(Long userId);

    @Query("SELECT t FROM Task t WHERE t.user.id = :userId AND t.title LIKE %:keyword%")
    List<Task> searchByTitleAndUserId(Long userId, String keyword);

    List<Task> findByUserIdOrderByDueDateAsc(Long userId);

    List<Task> findByUserIdOrderByPriorityAsc(Long userId);
}