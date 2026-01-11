package com.tradebytes.todo.service;

import com.tradebytes.todo.dto.TodoResponse;

import java.util.List;

/**
 * Service interface for Todo operations.
 */
public interface TodoService {

    /**
     * Get all todo items, optionally filtered by status.
     * 
     * @param includeAll if true, returns all items; if false, returns only "not done" items
     */
    List<TodoResponse> getAllTodos(boolean includeAll);
}
