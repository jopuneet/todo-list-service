package com.tradebytes.todo.mapper;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import org.springframework.stereotype.Component;

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
     * Uses entity's effective status for accurate representation.
     */
    public TodoResponse toResponse(TodoItem entity) {
        return TodoResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .status(entity.getEffectiveStatus().getValue())
                .creationDatetime(entity.getCreationDatetime())
                .dueDatetime(entity.getDueDatetime())
                .doneDatetime(entity.getDoneDatetime())
                .build();
    }
}
