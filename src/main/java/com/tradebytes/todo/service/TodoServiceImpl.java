package com.tradebytes.todo.service;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.dto.UpdateStatusRequest;
import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import com.tradebytes.todo.exception.TodoImmutableException;
import com.tradebytes.todo.exception.TodoNotFoundException;
import com.tradebytes.todo.mapper.TodoMapper;
import com.tradebytes.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of TodoService with business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    @Override
    public TodoResponse createTodo(CreateTodoRequest request) {
        log.info("Creating new todo item with description: {}", request.getDescription());
        
        TodoItem todoItem = todoMapper.toEntity(request);
        TodoItem savedItem = todoRepository.save(todoItem);
        
        log.info("Created todo item with id: {}", savedItem.getId());
        return todoMapper.toResponse(savedItem);
    }

    @Override
    @Transactional(readOnly = true)
    public TodoResponse getTodoById(Long id) {
        log.debug("Fetching todo item with id: {}", id);
        
        TodoItem todoItem = findTodoById(id);
        
        // Check if item should be marked as past due
        checkAndUpdatePastDue(todoItem);
        
        return todoMapper.toResponse(todoItem);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getAllTodos(boolean includeAll) {
        log.debug("Fetching all todos, includeAll: {}", includeAll);
        
        List<TodoItem> todos;
        if (includeAll) {
            todos = todoRepository.findAll();
        } else {
            todos = todoRepository.findByStatus(TodoStatus.NOT_DONE);
        }
        
        // Check and update past due items
        todos.forEach(this::checkAndUpdatePastDue);
        
        return todos.stream()
                .map(todoMapper::toResponse)
                .toList();
    }

    @Override
    public TodoResponse updateDescription(Long id, UpdateDescriptionRequest request) {
        log.info("Updating description for todo item with id: {}", id);
        
        TodoItem todoItem = findTodoById(id);
        validateNotPastDue(todoItem);
        
        todoItem.setDescription(request.getDescription());
        TodoItem updatedItem = todoRepository.save(todoItem);
        
        log.info("Updated description for todo item with id: {}", id);
        return todoMapper.toResponse(updatedItem);
    }

    @Override
    public TodoResponse updateStatus(Long id, UpdateStatusRequest request) {
        log.info("Updating status for todo item with id: {} to {}", id, request.getStatus());
        
        TodoItem todoItem = findTodoById(id);
        validateNotPastDue(todoItem);
        
        TodoStatus newStatus = TodoStatus.fromValue(request.getStatus());
        
        // Additional validation: cannot set status to PAST_DUE via API
        if (newStatus == TodoStatus.PAST_DUE) {
            throw new IllegalArgumentException("Cannot set status to 'past due' via API. This status is set automatically.");
        }
        
        todoItem.setStatus(newStatus);
        
        // Update done_datetime based on status
        if (newStatus == TodoStatus.DONE) {
            todoItem.setDoneDatetime(LocalDateTime.now());
        } else {
            todoItem.setDoneDatetime(null);
        }
        
        TodoItem updatedItem = todoRepository.save(todoItem);
        
        log.info("Updated status for todo item with id: {} to {}", id, newStatus.getValue());
        return todoMapper.toResponse(updatedItem);
    }

    @Override
    public void updatePastDueItems() {
        log.debug("Running scheduled past due items update");
        
        int updatedCount = todoRepository.updatePastDueItems(
                TodoStatus.NOT_DONE,
                TodoStatus.PAST_DUE,
                LocalDateTime.now()
        );
        
        if (updatedCount > 0) {
            log.info("Updated {} items to past due status", updatedCount);
        }
    }

    /**
     * Find a todo item by ID or throw exception.
     */
    private TodoItem findTodoById(Long id) {
        return todoRepository.findById(id)
                .orElseThrow(() -> new TodoNotFoundException(id));
    }

    /**
     * Validate that the todo item is not past due.
     */
    private void validateNotPastDue(TodoItem todoItem) {
        // First check if already marked as past due
        if (todoItem.getStatus() == TodoStatus.PAST_DUE) {
            throw new TodoImmutableException(todoItem.getId());
        }
        
        // Then check if it should be marked as past due
        if (todoItem.getStatus() == TodoStatus.NOT_DONE && 
            todoItem.getDueDatetime().isBefore(LocalDateTime.now())) {
            todoItem.setStatus(TodoStatus.PAST_DUE);
            todoRepository.save(todoItem);
            throw new TodoImmutableException(todoItem.getId());
        }
    }

    /**
     * Check if item is past due and update status if needed.
     */
    private void checkAndUpdatePastDue(TodoItem todoItem) {
        if (todoItem.getStatus() == TodoStatus.NOT_DONE && 
            todoItem.getDueDatetime().isBefore(LocalDateTime.now())) {
            todoItem.setStatus(TodoStatus.PAST_DUE);
            todoRepository.save(todoItem);
        }
    }
}
