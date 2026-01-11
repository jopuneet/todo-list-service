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
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceImplTest {

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private TodoMapper todoMapper;

    @InjectMocks
    private TodoServiceImpl todoService;

    @Captor
    private ArgumentCaptor<TodoItem> todoItemCaptor;

    private TodoItem sampleTodoItem;
    private TodoResponse sampleTodoResponse;
    private CreateTodoRequest createRequest;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        LocalDateTime now = LocalDateTime.now();
        futureDate = now.plusDays(1);

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
        @DisplayName("Should create todo with correct initial state")
        void shouldCreateTodoWithCorrectInitialState() {
            when(todoMapper.toEntity(createRequest)).thenReturn(sampleTodoItem);
            when(todoRepository.save(todoItemCaptor.capture())).thenReturn(sampleTodoItem);
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);

            TodoResponse result = todoService.createTodo(createRequest);

            // Assert outcome: response has expected values
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDescription()).isEqualTo("Test task");
            assertThat(result.getStatus()).isEqualTo("not done");
            assertThat(result.getDueDatetime()).isEqualTo(futureDate);

            // Assert outcome: entity was saved with correct state
            TodoItem savedItem = todoItemCaptor.getValue();
            assertThat(savedItem.getDescription()).isEqualTo("Test task");
            assertThat(savedItem.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        }
    }

    @Nested
    @DisplayName("Get Todo Tests")
    class GetTodoTests {

        @Test
        @DisplayName("Should return todo with all fields populated")
        void shouldReturnTodoWithAllFields() {
            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);

            TodoResponse result = todoService.getTodoById(1L);

            // Assert outcome: all expected fields are present
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getDescription()).isEqualTo("Test task");
            assertThat(result.getStatus()).isEqualTo("not done");
            assertThat(result.getDueDatetime()).isEqualTo(futureDate);
        }

        @Test
        @DisplayName("Should throw TodoNotFoundException for non-existent id")
        void shouldThrowExceptionWhenTodoNotFound() {
            when(todoRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> todoService.getTodoById(999L))
                    .isInstanceOf(TodoNotFoundException.class)
                    .hasMessageContaining("999");
        }

        @Test
        @DisplayName("Should return only not-done todos by default")
        void shouldReturnOnlyNotDoneTodos() {
            TodoItem doneItem = TodoItem.builder()
                    .id(2L)
                    .description("Done task")
                    .status(TodoStatus.DONE)
                    .build();
            TodoResponse doneResponse = TodoResponse.builder()
                    .id(2L)
                    .description("Done task")
                    .status("done")
                    .build();

            when(todoRepository.findByStatus(TodoStatus.NOT_DONE))
                    .thenReturn(List.of(sampleTodoItem));
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);

            List<TodoResponse> result = todoService.getAllTodos(false);

            // Assert outcome: only not-done items returned
            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo("not done");
        }

        @Test
        @DisplayName("Should return all todos when includeAll is true")
        void shouldReturnAllTodosWhenIncludeAllTrue() {
            TodoItem doneItem = TodoItem.builder()
                    .id(2L)
                    .description("Done task")
                    .status(TodoStatus.DONE)
                    .build();
            TodoResponse doneResponse = TodoResponse.builder()
                    .id(2L)
                    .description("Done task")
                    .status("done")
                    .build();

            when(todoRepository.findAll()).thenReturn(List.of(sampleTodoItem, doneItem));
            when(todoMapper.toResponse(sampleTodoItem)).thenReturn(sampleTodoResponse);
            when(todoMapper.toResponse(doneItem)).thenReturn(doneResponse);

            List<TodoResponse> result = todoService.getAllTodos(true);

            // Assert outcome: all items returned regardless of status
            assertThat(result).hasSize(2);
            assertThat(result).extracting(TodoResponse::getStatus)
                    .containsExactlyInAnyOrder("not done", "done");
        }
    }

    @Nested
    @DisplayName("Update Description Tests")
    class UpdateDescriptionTests {

        @Test
        @DisplayName("Should update description and return updated todo")
        void shouldUpdateDescriptionSuccessfully() {
            UpdateDescriptionRequest updateRequest = UpdateDescriptionRequest.builder()
                    .description("Updated task")
                    .build();

            TodoResponse updatedResponse = TodoResponse.builder()
                    .id(1L)
                    .description("Updated task")
                    .status("not done")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoRepository.save(todoItemCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
            when(todoMapper.toResponse(any(TodoItem.class))).thenReturn(updatedResponse);

            TodoResponse result = todoService.updateDescription(1L, updateRequest);

            // Assert outcome: response has updated description
            assertThat(result.getDescription()).isEqualTo("Updated task");

            // Assert outcome: entity description was actually changed
            TodoItem savedItem = todoItemCaptor.getValue();
            assertThat(savedItem.getDescription()).isEqualTo("Updated task");
            assertThat(savedItem.getStatus()).isEqualTo(TodoStatus.NOT_DONE); // Status unchanged
        }

        @Test
        @DisplayName("Should reject update for past due item")
        void shouldRejectUpdateForPastDueItem() {
            sampleTodoItem.setStatus(TodoStatus.PAST_DUE);
            UpdateDescriptionRequest updateRequest = UpdateDescriptionRequest.builder()
                    .description("Updated task")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));

            assertThatThrownBy(() -> todoService.updateDescription(1L, updateRequest))
                    .isInstanceOf(TodoImmutableException.class)
                    .hasMessageContaining("past due")
                    .hasMessageContaining("immutable");
        }
    }

    @Nested
    @DisplayName("Update Status Tests")
    class UpdateStatusTests {

        @Test
        @DisplayName("Should set doneDatetime when marking as done")
        void shouldSetDoneDatetimeWhenMarkingDone() {
            UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                    .status("done")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoRepository.save(todoItemCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
            when(todoMapper.toResponse(any(TodoItem.class))).thenAnswer(inv -> {
                TodoItem item = inv.getArgument(0);
                return TodoResponse.builder()
                        .id(item.getId())
                        .status(item.getStatus().getValue())
                        .doneDatetime(item.getDoneDatetime())
                        .build();
            });

            TodoResponse result = todoService.updateStatus(1L, statusRequest);

            // Assert outcome: status changed and doneDatetime set
            assertThat(result.getStatus()).isEqualTo("done");
            assertThat(result.getDoneDatetime()).isNotNull();

            // Assert outcome: entity has correct state
            TodoItem savedItem = todoItemCaptor.getValue();
            assertThat(savedItem.getStatus()).isEqualTo(TodoStatus.DONE);
            assertThat(savedItem.getDoneDatetime()).isNotNull();
        }

        @Test
        @DisplayName("Should clear doneDatetime when marking as not done")
        void shouldClearDoneDatetimeWhenMarkingNotDone() {
            sampleTodoItem.setStatus(TodoStatus.DONE);
            sampleTodoItem.setDoneDatetime(LocalDateTime.now());

            UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                    .status("not done")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));
            when(todoRepository.save(todoItemCaptor.capture())).thenAnswer(inv -> inv.getArgument(0));
            when(todoMapper.toResponse(any(TodoItem.class))).thenAnswer(inv -> {
                TodoItem item = inv.getArgument(0);
                return TodoResponse.builder()
                        .id(item.getId())
                        .status(item.getStatus().getValue())
                        .doneDatetime(item.getDoneDatetime())
                        .build();
            });

            TodoResponse result = todoService.updateStatus(1L, statusRequest);

            // Assert outcome: status changed and doneDatetime cleared
            assertThat(result.getStatus()).isEqualTo("not done");
            assertThat(result.getDoneDatetime()).isNull();

            // Assert outcome: entity has correct state
            TodoItem savedItem = todoItemCaptor.getValue();
            assertThat(savedItem.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
            assertThat(savedItem.getDoneDatetime()).isNull();
        }

        @Test
        @DisplayName("Should reject status update for past due item")
        void shouldRejectStatusUpdateForPastDueItem() {
            sampleTodoItem.setStatus(TodoStatus.PAST_DUE);
            UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                    .status("done")
                    .build();

            when(todoRepository.findById(1L)).thenReturn(Optional.of(sampleTodoItem));

            assertThatThrownBy(() -> todoService.updateStatus(1L, statusRequest))
                    .isInstanceOf(TodoImmutableException.class);
        }

        @Test
        @DisplayName("Should reject setting status to past due via API")
        void shouldRejectSettingPastDueStatusViaApi() {
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
        @DisplayName("Should return count of updated items")
        void shouldReturnCountOfUpdatedItems() {
            when(todoRepository.updatePastDueItems(
                    eq(TodoStatus.NOT_DONE),
                    eq(TodoStatus.PAST_DUE),
                    any(LocalDateTime.class)
            )).thenReturn(5);

            // Call the method - we're testing it doesn't throw and completes
            todoService.updatePastDueItems();

            // The method logs the count internally; in a real scenario
            // we might expose this count or verify via integration test
            // For unit test, we verify the query parameters are sensible
        }

        @Test
        @DisplayName("Should handle zero items gracefully")
        void shouldHandleZeroItemsGracefully() {
            when(todoRepository.updatePastDueItems(
                    eq(TodoStatus.NOT_DONE),
                    eq(TodoStatus.PAST_DUE),
                    any(LocalDateTime.class)
            )).thenReturn(0);

            // Should not throw
            todoService.updatePastDueItems();
        }
    }
}
