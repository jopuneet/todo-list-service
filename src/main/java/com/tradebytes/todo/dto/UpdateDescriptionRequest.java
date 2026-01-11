package com.tradebytes.todo.dto;

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
public class UpdateDescriptionRequest {

    @NotBlank(message = "Description is required")
    private String description;
}
