package com.tradebytes.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a Todo item's description.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating a todo item's description")
public class UpdateDescriptionRequest {

    @NotBlank(message = "Description is required")
    @Schema(
            description = "The new description for the todo item",
            example = "Updated task description",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String description;
}
