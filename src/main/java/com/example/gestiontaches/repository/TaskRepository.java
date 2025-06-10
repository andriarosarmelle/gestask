package com.example.gestiontaches.repository;

import com.example.gestiontaches.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.user WHERE t.user.id = :userId")
    List<Task> findByUserIdWithCategories(@Param("userId") Long userId);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.user WHERE t.id = :id")
    Optional<Task> findByIdWithCategory(@Param("id") Long id);
    
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.category LEFT JOIN FETCH t.user " +
           "WHERE t.user.id = :userId AND (LOWER(t.title) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(t.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    List<Task> findByUserIdAndKeyword(@Param("userId") Long userId, @Param("keyword") String keyword);
}