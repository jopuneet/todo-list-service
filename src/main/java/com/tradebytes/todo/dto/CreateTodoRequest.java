package com.tradebytes.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
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
@Schema(description = "Request body for creating a new todo item")
public class CreateTodoRequest {

    @NotBlank(message = "Description is required")
    @Schema(
            description = "A string detailing the task",
            example = "Complete the coding challenge",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;

    @NotNull(message = "Due datetime is required")
    @JsonProperty("due_datetime")
    @Schema(
            description = "The timestamp by which the task should be completed",
            example = "2026-01-15T18:00:00",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private LocalDateTime dueDatetime;
}
