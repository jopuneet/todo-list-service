package com.tradebytes.todo.controller;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.dto.UpdateStatusRequest;
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

    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse response = todoService.createTodo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(@PathVariable Long id) {
        TodoResponse response = todoService.getTodoById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos(
            @RequestParam(name = "all", defaultValue = "false") boolean includeAll) {
        List<TodoResponse> response = todoService.getAllTodos(includeAll);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/description")
    public ResponseEntity<TodoResponse> updateDescription(
            @PathVariable Long id,
            @Valid @RequestBody UpdateDescriptionRequest request) {
        TodoResponse response = todoService.updateDescription(id, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TodoResponse> updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateStatusRequest request) {
        TodoResponse response = todoService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }
}
