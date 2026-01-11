package com.tradebytes.todo.controller;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.service.TodoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Todo operations.
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    /**
     * Create a new todo item.
     * <p>
     * POST /api/todos
     */
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse response = todoService.createTodo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a todo item by ID.
     * <p>
     * GET /api/todos/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(@PathVariable Long id) {
        TodoResponse response = todoService.getTodoById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all todo items.
     * By default, returns only "not done" items.
     * Use ?all=true to get all items regardless of status.
     * <p>
     * GET /api/todos
     * GET /api/todos?all=true
     */
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos(
            @RequestParam(name = "all", defaultValue = "false") boolean includeAll) {
        List<TodoResponse> response = todoService.getAllTodos(includeAll);
        return ResponseEntity.ok(response);
    }

    /**
     * Update the description of a todo item.
     * <p>
     * PATCH /api/todos/{id}/description
     */
    @PatchMapping("/{id}/description")
    public ResponseEntity<TodoResponse> updateDescription(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDescriptionRequest request) {
        TodoResponse response = todoService.updateDescription(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a todo item as done.
     * <p>
     * PATCH /api/todos/{id}/done
     */
    @PatchMapping("/{id}/done")
    public ResponseEntity<TodoResponse> markAsDone(@PathVariable Long id) {
        TodoResponse response = todoService.markAsDone(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a todo item as not done.
     * <p>
     * PATCH /api/todos/{id}/not-done
     */
    @PatchMapping("/{id}/not-done")
    public ResponseEntity<TodoResponse> markAsNotDone(@PathVariable Long id) {
        TodoResponse response = todoService.markAsNotDone(id);
        return ResponseEntity.ok(response);
    }
}
