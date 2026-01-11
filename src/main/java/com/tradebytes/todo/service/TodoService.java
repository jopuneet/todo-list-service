package com.tradebytes.todo.service;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.dto.UpdateStatusRequest;

import java.util.List;

/**
 * Service interface for Todo operations.
 */
public interface TodoService {

    /**
     * Create a new todo item.
     */
    TodoResponse createTodo(CreateTodoRequest request);

    /**
     * Get a todo item by its ID.
     */
    TodoResponse getTodoById(Long id);

    /**
     * Get all todo items, optionally filtered by status.
     * 
     * @param includeAll if true, returns all items; if false, returns only "not done" items
     */
    List<TodoResponse> getAllTodos(boolean includeAll);

    /**
     * Update the description of a todo item.
     */
    TodoResponse updateDescription(Long id, UpdateDescriptionRequest request);

    /**
     * Update the status of a todo item.
     * Only allows changing to 'done' or 'not done'.
     */
    TodoResponse updateStatus(Long id, UpdateStatusRequest request);

    /**
     * Update status of all past due items.
     * Called by the scheduler.
     */
    void updatePastDueItems();
}
