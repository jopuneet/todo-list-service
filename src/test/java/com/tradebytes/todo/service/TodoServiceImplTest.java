package com.tradebytes.todo.service;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.dto.UpdateStatusRequest;
import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import com.tradebytes.todo.exception.TodoImmutableException;
import com.tradebytes.todo.exception.TodoNotFoundException;
import com.tradebytes.todo.mapper.TodoMapper;
import com.tradebytes.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoServiceImpl todoService;

    private TodoItem sampleTodoItem;
    private TodoResponse sampleTodoResponse;
    private CreateTodoRequest createRequest;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime futureDate = now.plusDays(1);

        sampleTodoItem = TodoItem.builder()
                .id(1L)
                .description("Test task")
                .status(TodoStatus.NOT_DONE)
                .creationDatetime(now)
                .dueDatetime(futureDate)
                .build();

        sampleTodoResponse = TodoResponse.builder()
                .id(1L)
                .description("Test task")
                .status("not done")
                .creationDatetime(now)
                .dueDatetime(futureDate)
                .build();

        createRequest = CreateTodoRequest.builder()
                .description("Test task")
                .dueDatetime(futureDate)
                .build();
    }

    @Nested
    @DisplayName("Create Todo Tests")
    class CreateTodoTests {

        @Test
        @DisplayName("Should create a new todo item successfully")
        void shouldCreateTodoSuccessfully() {
            when(todoMapper.toEntity(createRequest)).thenReturn(sampleTodoItem);
            when(todoRepository.save(sampleTodoItem)).thenReturn(sampleTodoItem);
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);

            TodoResponse result = todoService.createTodo(createRequest);

            assertThat(result).isNotNull();
            assertThat(result.getDescription()).isEqualTo("Test task");
            assertThat(result.getStatus()).isEqualTo("not done");
            verify(todoRepository).save(any(TodoItem.class));
        }
    }

    @Nested
    @DisplayName("Get Todo Tests")
    class GetTodoTests {

        @Test
        @DisplayName("Should get todo by ID successfully")
        void shouldGetTodoByIdSuccessfully() {
            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);

            TodoResponse result = todoService.getTodoById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            verify(todoRepository).findById(1L);
        }

        @Test
        @DisplayName("Should throw exception when todo not found")
        void shouldThrowExceptionWhenTodoNotFound() {
            when(todoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.getTodoById(999L))
                    .isInstanceOf(TodoNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should get all not done todos")
        void shouldGetAllNotDoneTodos() {
            when(todoRepository.findByStatus(TodoStatus.NOT_DONE)).thenReturn(List.of(sampleTodoItem));
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);

            List<TodoResponse> result = todoService.getAllTodos(false);

            assertThat(result).hasSize(1);
            verify(todoRepository).findByStatus(TodoStatus.NOT_DONE);
            verify(todoRepository, never()).findAll();
        }

        @Test
        @DisplayName("Should get all todos when includeAll is true")
        void shouldGetAllTodosWhenIncludeAllTrue() {
            when(todoRepository.findAll()).thenReturn(List.of(sampleTodoItem));
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);

            List<TodoResponse> result = todoService.getAllTodos(true);

            assertThat(result).hasSize(1);
            verify(todoRepository).findAll();
            verify(todoRepository, never()).findByStatus(any());
        }
    }

    @Nested
    @DisplayName("Update Description Tests")
    class UpdateDescriptionTests {

        @Test
        @DisplayName("Should update description successfully")
        void shouldUpdateDescriptionSuccessfully() {
            UpdateDescriptionRequest updateRequest = UpdateDescriptionRequest.builder()
                    .description("Updated task")
                    .build();

            TodoItem updatedItem = TodoItem.builder()
                    .id(1L)
                    .description("Updated task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(sampleTodoItem.getCreationDatetime())
                    .dueDatetime(sampleTodoItem.getDueDatetime())
                    .build();

            TodoResponse updatedResponse = TodoResponse.builder()
                    .id(1L)
                    .description("Updated task")
                    .status("not done")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoRepository.save(any(TodoItem.class))).thenReturn(updatedItem);
            when(todoMapper.toResponse(any(TodoItem.class))).thenReturn(updatedResponse);

            TodoResponse result = todoService.updateDescription(1L, updateRequest);

            assertThat(result.getDescription()).isEqualTo("Updated task");
            verify(todoRepository).save(any(TodoItem.class));
        }

        @Test
        @DisplayName("Should throw exception when updating past due item")
        void shouldThrowExceptionWhenUpdatingPastDueItem() {
            sampleTodoItem.setStatus(TodoStatus.PAST_DUE);
            UpdateDescriptionRequest updateRequest = UpdateDescriptionRequest.builder()
                    .description("Updated task")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));

            assertThatThrownBy(() -> todoService.updateDescription(1L, updateRequest))
                    .isInstanceOf(TodoImmutableException.class);
        }
    }

    @Nested
    @DisplayName("Update Status Tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should update status to done successfully")
        void shouldUpdateStatusToDoneSuccessfully() {
            UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                    .status("done")
                    .build();

            TodoItem doneItem = TodoItem.builder()
                    .id(1L)
                    .description("Test task")
                    .status(TodoStatus.DONE)
                    .creationDatetime(sampleTodoItem.getCreationDatetime())
                    .dueDatetime(sampleTodoItem.getDueDatetime())
                    .doneDatetime(LocalDateTime.now())
                    .build();

            TodoResponse doneResponse = TodoResponse.builder()
                    .id(1L)
                    .description("Test task")
                    .status("done")
                    .doneDatetime(LocalDateTime.now())
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoRepository.save(any(TodoItem.class))).thenReturn(doneItem);
            when(todoMapper.toResponse(any(TodoItem.class))).thenReturn(doneResponse);

            TodoResponse result = todoService.updateStatus(1L, statusRequest);

            assertThat(result.getStatus()).isEqualTo("done");
            assertThat(result.getDoneDatetime()).isNotNull();
        }

        @Test
        @DisplayName("Should update status to not done successfully")
        void shouldUpdateStatusToNotDoneSuccessfully() {
            sampleTodoItem.setStatus(TodoStatus.DONE);
            sampleTodoItem.setDoneDatetime(LocalDateTime.now());

            UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                    .status("not done")
                    .build();

            TodoItem notDoneItem = TodoItem.builder()
                    .id(1L)
                    .description("Test task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(sampleTodoItem.getCreationDatetime())
                    .dueDatetime(sampleTodoItem.getDueDatetime())
                    .doneDatetime(null)
                    .build();

            TodoResponse notDoneResponse = TodoResponse.builder()
                    .id(1L)
                    .description("Test task")
                    .status("not done")
                    .doneDatetime(null)
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoRepository.save(any(TodoItem.class))).thenReturn(notDoneItem);
            when(todoMapper.toResponse(any(TodoItem.class))).thenReturn(notDoneResponse);

            TodoResponse result = todoService.updateStatus(1L, statusRequest);

            assertThat(result.getStatus()).isEqualTo("not done");
            assertThat(result.getDoneDatetime()).isNull();
        }

        @Test
        @DisplayName("Should throw exception when updating past due item status")
        void shouldThrowExceptionWhenUpdatingPastDueItemStatus() {
            sampleTodoItem.setStatus(TodoStatus.PAST_DUE);
            UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                    .status("done")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));

            assertThatThrownBy(() -> todoService.updateStatus(1L, statusRequest))
                    .isInstanceOf(TodoImmutableException.class);
        }

        @Test
        @DisplayName("Should throw exception when trying to set past due status via API")
        void shouldThrowExceptionWhenSettingPastDueStatus() {
            UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                    .status("past due")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));

            assertThatThrownBy(() -> todoService.updateStatus(1L, statusRequest))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Cannot set status to 'past due' via API");
        }
    }

    @Nested
    @DisplayName("Update Past Due Items Tests")
    class UpdatePastDueItemsTests {

        @Test
        @DisplayName("Should update past due items")
        void shouldUpdatePastDueItems() {
            when(todoRepository.updatePastDueItems(
                    eq(TodoStatus.NOT_DONE),
                    eq(TodoStatus.PAST_DUE),
                    any(LocalDateTime.class)
            )).thenReturn(2);

            todoService.updatePastDueItems();

            verify(todoRepository).updatePastDueItems(
                    eq(TodoStatus.NOT_DONE),
                    eq(TodoStatus.PAST_DUE),
                    any(LocalDateTime.class)
            );
        }
    }
}
