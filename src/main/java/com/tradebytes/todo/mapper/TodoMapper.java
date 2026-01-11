package com.tradebytes.todo.mapper;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Mapper for converting between TodoItem entity and DTOs.
 */
@Component
public class TodoMapper {

    /**
     * Convert CreateTodoRequest DTO to TodoItem entity.
     */
    public TodoItem toEntity(CreateTodoRequest request) {
        return TodoItem.builder()
                .description(request.getDescription())
                .dueDatetime(request.getDueDatetime())
                .status(TodoStatus.NOT_DONE)
                .build();
    }

    /**
     * Convert TodoItem entity to TodoResponse DTO.
     * Computes effective status: if item is NOT_DONE but due date has passed,
     * returns "past due" in the response (without modifying the entity).
     */
    public TodoResponse toResponse(TodoItem entity) {
        return TodoResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .status(computeEffectiveStatus(entity))
                .creationDatetime(entity.getCreationDatetime())
                .dueDatetime(entity.getDueDatetime())
                .doneDatetime(entity.getDoneDatetime())
                .build();
    }

    /**
     * Compute the effective status for display purposes.
     * Items that are NOT_DONE but past their due date are shown as "past due".
     */
    private String computeEffectiveStatus(TodoItem entity) {
        if (entity.getStatus() == TodoStatus.NOT_DONE && isPastDue(entity)) {
            return TodoStatus.PAST_DUE.getValue();
        }
        return entity.getStatus().getValue();
    }

    /**
     * Check if an item is past its due date.
     */
    private boolean isPastDue(TodoItem entity) {
        return entity.getDueDatetime() != null
                && entity.getDueDatetime().isBefore(LocalDateTime.now());
    }
}
