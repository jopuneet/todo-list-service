package com.tradebytes.todo.exception;

/**
 * Exception thrown when attempting to modify a Todo item that is past due.
 */
public class TodoImmutableException extends RuntimeException {

    public TodoImmutableException(Long id) {
        super("Cannot modify todo item with id: " + id + ". Item is past due and immutable.");
    }

    public TodoImmutableException(String message) {
        super(message);
    }
}
