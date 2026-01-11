package com.tradebytes.todo.controller;

import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
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
}
