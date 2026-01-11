package com.tradebytes.todo.service;

import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import com.tradebytes.todo.mapper.TodoMapper;
import com.tradebytes.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Implementation of TodoService with business logic.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TodoServiceImpl implements TodoService {

    private final TodoRepository todoRepository;
    private final TodoMapper todoMapper;

    @Override
    @Transactional(readOnly = true)
    public List<TodoResponse> getAllTodos(boolean includeAll) {
        log.debug("Fetching all todos, includeAll: {}", includeAll);
        
        List<TodoItem> todos;
        if (includeAll) {
            todos = todoRepository.findAll();
        } else {
            todos = todoRepository.findByStatus(TodoStatus.NOT_DONE);
        }
        
        // Check and update past due items
        todos.forEach(this::checkAndUpdatePastDue);
        
        return todos.stream()
                .map(todoMapper::toResponse)
                .toList();
    }

    /**
     * Check if item is past due and update status if needed.
     */
    private void checkAndUpdatePastDue(TodoItem todoItem) {
        if (todoItem.getStatus() == TodoStatus.NOT_DONE &&
            todoItem.getDueDatetime().isBefore(LocalDateTime.now())) {
            todoItem.setStatus(TodoStatus.PAST_DUE);
            todoRepository.save(todoItem);
        }
    }
}
