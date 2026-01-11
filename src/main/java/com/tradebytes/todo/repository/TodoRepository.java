package com.tradebytes.todo.repository;

import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for TodoItem entity operations.
 */
@Repository
public interface TodoRepository extends JpaRepository<TodoItem, Long> {

    /**
     * Find all todo items with a specific status.
     */
    List<TodoItem> findByStatus(TodoStatus status);
}
