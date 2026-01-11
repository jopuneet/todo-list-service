package com.tradebytes.todo.entity;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * Enum representing the possible statuses of a Todo item.
 */
public enum TodoStatus {
    NOT_DONE("not done"),
    DONE("done"),
    PAST_DUE("past due");

    private final String value;

    TodoStatus(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    public static TodoStatus fromValue(String value) {
        for (TodoStatus status : TodoStatus.values()) {
            if (status.value.equalsIgnoreCase(value)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown status: " + value);
    }
}
