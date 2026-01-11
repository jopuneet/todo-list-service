package com.tradebytes.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for creating a new Todo item.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateTodoRequest {

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Due datetime is required")
    @JsonProperty("due_datetime")
    private LocalDateTime dueDatetime;
}
