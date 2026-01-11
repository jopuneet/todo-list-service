package com.tradebytes.todo.exception;

/**
 * Exception thrown when a requested Todo item is not found.
 */
public class TodoNotFoundException extends RuntimeException {

    public TodoNotFoundException(Long id) {
        super("Todo item not found with id: " + id);
    }

    public TodoNotFoundException(String message) {
        super(message);
    }
}
