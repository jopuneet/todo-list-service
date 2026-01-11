package com.tradebytes.todo.mapper;

import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.entity.TodoItem;
import org.springframework.stereotype.Component;

/**
 * Mapper for converting between TodoItem entity and DTOs.
 */
@Component
public class TodoMapper {

    /**
     * Convert TodoItem entity to TodoResponse DTO.
     */
    public TodoResponse toResponse(TodoItem entity) {
        return TodoResponse.builder()
                .id(entity.getId())
                .description(entity.getDescription())
                .status(entity.getStatus().getValue())
                .creationDatetime(entity.getCreationDatetime())
                .dueDatetime(entity.getDueDatetime())
                .doneDatetime(entity.getDoneDatetime())
                .build();
    }
}
