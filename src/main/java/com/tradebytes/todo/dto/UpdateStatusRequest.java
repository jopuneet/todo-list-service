package com.tradebytes.todo.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating a Todo item's status.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Request body for updating a todo item's status")
public class UpdateStatusRequest {

    @NotBlank(message = "Status is required")
    @Pattern(regexp = "^(done|not done)$", message = "Status must be either 'done' or 'not done'")
    @Schema(
            description = "The new status for the todo item. Only 'done' and 'not done' are allowed. " +
                    "Setting status to 'past due' is not permitted via API.",
            example = "done",
            allowableValues = {"done", "not done"},
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private String status;
}
