package com.tradebytes.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Todo item responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Todo item response")
public class TodoResponse {

    @Schema(description = "Unique identifier of the todo item", example = "1")
    private Long id;

    @Schema(description = "A string detailing the task", example = "Complete the coding challenge")
    private String description;

    @Schema(
            description = "Current status of the todo item",
            example = "not done",
            allowableValues = {"not done", "done", "past due"}
    )
    private String status;

    @JsonProperty("creation_datetime")
    @Schema(description = "The timestamp when the item was created", example = "2026-01-11T10:30:00")
    private LocalDateTime creationDatetime;

    @JsonProperty("due_datetime")
    @Schema(description = "The timestamp by which the task should be completed", example = "2026-01-15T18:00:00")
    private LocalDateTime dueDatetime;

    @JsonProperty("done_datetime")
    @Schema(
            description = "The timestamp when the item was marked as 'done' (null if not done)",
            example = "2026-01-12T14:00:00",
            nullable = true
    )
    private LocalDateTime doneDatetime;
}
