package com.example.todoapp.repository;

import com.example.todoapp.model.Todo;
import com.example.todoapp.model.TodoStatus;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TodoRepository extends JpaRepository<Todo, UUID> {

    @Query("""
            SELECT t FROM Todo t
            WHERE (:status IS NULL OR t.status = :status)
              AND (:fromDate IS NULL OR t.dueDate >= :fromDate)
              AND (:toDate IS NULL OR t.dueDate <= :toDate)
            ORDER BY t.createdAt DESC
            """)
    List<Todo> search(
            @Param("status") TodoStatus status,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
}
