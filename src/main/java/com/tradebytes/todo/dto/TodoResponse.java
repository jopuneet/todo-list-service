package com.tradebytes.todo.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class TodoResponse {

    private Long id;

    private String description;

    private String status;

    @JsonProperty("creation_datetime")
    private LocalDateTime creationDatetime;

    @JsonProperty("due_datetime")
    private LocalDateTime dueDatetime;

    @JsonProperty("done_datetime")
    private LocalDateTime doneDatetime;
}
