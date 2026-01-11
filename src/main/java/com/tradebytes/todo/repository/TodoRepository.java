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

    /**
     * Find all todo items that are past due and still marked as NOT_DONE.
     */
    @Query("SELECT t FROM TodoItem t WHERE t.status = :status AND t.dueDatetime < :now")
    List<TodoItem> findPastDueItems(@Param("status") TodoStatus status, @Param("now") LocalDateTime now);

    /**
     * Update status of all past due items in bulk.
     */
    @Modifying
    @Query("UPDATE TodoItem t SET t.status = :newStatus WHERE t.status = :oldStatus AND t.dueDatetime < :now")
    int updatePastDueItems(@Param("oldStatus") TodoStatus oldStatus, 
                           @Param("newStatus") TodoStatus newStatus, 
                           @Param("now") LocalDateTime now);
}
