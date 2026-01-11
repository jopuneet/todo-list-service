package com.tradebytes.todo.mapper;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TodoMapper.
 */
class TodoMapperTest {

    private TodoMapper todoMapper;

    @BeforeEach
    void setUp() {
        todoMapper = new TodoMapper();
    }

    @Nested
    @DisplayName("toEntity() Tests")
    class ToEntityTests {

        @Test
        @DisplayName("Should create entity with NOT_DONE status")
        void shouldCreateEntityWithNotDoneStatus() {
            CreateTodoRequest request = CreateTodoRequest.builder()
                    .description("Test task")
                    .dueDatetime(LocalDateTime.now().plusDays(1))
                    .build();

            TodoItem entity = todoMapper.toEntity(request);

            assertThat(entity.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
            assertThat(entity.getDescription()).isEqualTo("Test task");
            assertThat(entity.getDueDatetime()).isEqualTo(request.getDueDatetime());
            assertThat(entity.getId()).isNull(); // Not persisted yet
        }
    }

    @Nested
    @DisplayName("toResponse() Tests")
    class ToResponseTests {

        @Test
        @DisplayName("Should map all fields correctly for NOT_DONE item")
        void shouldMapAllFieldsForNotDoneItem() {
            LocalDateTime now = LocalDateTime.now();
            TodoItem entity = TodoItem.builder()
                    .id(1L)
                    .description("Test task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(now.minusDays(1))
                    .dueDatetime(now.plusDays(1))
                    .build();

            TodoResponse response = todoMapper.toResponse(entity);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getDescription()).isEqualTo("Test task");
            assertThat(response.getStatus()).isEqualTo("not done");
            assertThat(response.getCreationDatetime()).isEqualTo(now.minusDays(1));
            assertThat(response.getDueDatetime()).isEqualTo(now.plusDays(1));
            assertThat(response.getDoneDatetime()).isNull();
        }

        @Test
        @DisplayName("Should return 'past due' status for overdue NOT_DONE item")
        void shouldReturnPastDueStatusForOverdueItem() {
            TodoItem entity = TodoItem.builder()
                    .id(1L)
                    .description("Overdue task")
                    .status(TodoStatus.NOT_DONE) // DB status is NOT_DONE
                    .creationDatetime(LocalDateTime.now().minusDays(2))
                    .dueDatetime(LocalDateTime.now().minusHours(1)) // But due date passed
                    .build();

            TodoResponse response = todoMapper.toResponse(entity);

            // Response should show "past due" even though DB has NOT_DONE
            assertThat(response.getStatus()).isEqualTo("past due");
        }

        @Test
        @DisplayName("Should return 'done' status for DONE item even if past due date")
        void shouldReturnDoneStatusForDoneItem() {
            LocalDateTime now = LocalDateTime.now();
            TodoItem entity = TodoItem.builder()
                    .id(1L)
                    .description("Completed task")
                    .status(TodoStatus.DONE)
                    .creationDatetime(now.minusDays(3))
                    .dueDatetime(now.minusDays(1)) // Due date passed
                    .doneDatetime(now.minusDays(2)) // But completed before due date
                    .build();

            TodoResponse response = todoMapper.toResponse(entity);

            assertThat(response.getStatus()).isEqualTo("done");
            assertThat(response.getDoneDatetime()).isEqualTo(now.minusDays(2));
        }

        @Test
        @DisplayName("Should return 'past due' status for PAST_DUE item")
        void shouldReturnPastDueStatusForPastDueItem() {
            TodoItem entity = TodoItem.builder()
                    .id(1L)
                    .description("Past due task")
                    .status(TodoStatus.PAST_DUE)
                    .creationDatetime(LocalDateTime.now().minusDays(5))
                    .dueDatetime(LocalDateTime.now().minusDays(2))
                    .build();

            TodoResponse response = todoMapper.toResponse(entity);

            assertThat(response.getStatus()).isEqualTo("past due");
        }

        @Test
        @DisplayName("Should handle null due datetime gracefully")
        void shouldHandleNullDueDatetimeGracefully() {
            TodoItem entity = TodoItem.builder()
                    .id(1L)
                    .description("Task without due date")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now())
                    .dueDatetime(null) // No due date
                    .build();

            TodoResponse response = todoMapper.toResponse(entity);

            assertThat(response.getStatus()).isEqualTo("not done");
            assertThat(response.getDueDatetime()).isNull();
        }
    }
}
