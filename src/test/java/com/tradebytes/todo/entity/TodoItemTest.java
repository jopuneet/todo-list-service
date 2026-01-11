package com.tradebytes.todo.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for TodoItem entity business logic.
 * Tests the isEffectivelyPastDue() and getEffectiveStatus() methods.
 */
class TodoItemTest {

    @Nested
    @DisplayName("isEffectivelyPastDue() Tests")
    class IsEffectivelyPastDueTests {

        @Test
        @DisplayName("Should return true when status is PAST_DUE")
        void shouldReturnTrueWhenStatusIsPastDue() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.PAST_DUE)
                    .dueDatetime(LocalDateTime.now().plusDays(1)) // Future date, but status is PAST_DUE
                    .build();

            assertThat(item.isEffectivelyPastDue()).isTrue();
        }

        @Test
        @DisplayName("Should return true when NOT_DONE and due date is in the past")
        void shouldReturnTrueWhenNotDoneAndDueDatePassed() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(LocalDateTime.now().minusMinutes(1))
                    .build();

            assertThat(item.isEffectivelyPastDue()).isTrue();
        }

        @Test
        @DisplayName("Should return false when NOT_DONE and due date is in the future")
        void shouldReturnFalseWhenNotDoneAndDueDateFuture() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(LocalDateTime.now().plusDays(1))
                    .build();

            assertThat(item.isEffectivelyPastDue()).isFalse();
        }

        @Test
        @DisplayName("Should return false when status is DONE regardless of due date")
        void shouldReturnFalseWhenDoneRegardlessOfDueDate() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.DONE)
                    .dueDatetime(LocalDateTime.now().minusDays(1)) // Past due date
                    .doneDatetime(LocalDateTime.now())
                    .build();

            assertThat(item.isEffectivelyPastDue()).isFalse();
        }

        @Test
        @DisplayName("Should return false when NOT_DONE and due datetime is null")
        void shouldReturnFalseWhenDueDatetimeIsNull() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(null)
                    .build();

            assertThat(item.isEffectivelyPastDue()).isFalse();
        }

        @Test
        @DisplayName("Should handle boundary condition - due datetime slightly in future")
        void shouldNotBePastDueWhenDueDatetimeIsSlightlyInFuture() {
            // Due datetime is 1 second in the future - should NOT be past due
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(LocalDateTime.now().plusSeconds(1))
                    .build();

            assertThat(item.isEffectivelyPastDue()).isFalse();
        }

        @Test
        @DisplayName("Should return true when due datetime is 1 second in the past")
        void shouldReturnTrueWhenDueDatetimeJustPassed() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(LocalDateTime.now().minusSeconds(1))
                    .build();

            assertThat(item.isEffectivelyPastDue()).isTrue();
        }
    }

    @Nested
    @DisplayName("getEffectiveStatus() Tests")
    class GetEffectiveStatusTests {

        @Test
        @DisplayName("Should return PAST_DUE when item is effectively past due")
        void shouldReturnPastDueWhenEffectivelyPastDue() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(LocalDateTime.now().minusHours(1))
                    .build();

            assertThat(item.getEffectiveStatus()).isEqualTo(TodoStatus.PAST_DUE);
        }

        @Test
        @DisplayName("Should return NOT_DONE when item is not past due")
        void shouldReturnNotDoneWhenNotPastDue() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(LocalDateTime.now().plusDays(1))
                    .build();

            assertThat(item.getEffectiveStatus()).isEqualTo(TodoStatus.NOT_DONE);
        }

        @Test
        @DisplayName("Should return DONE when status is DONE")
        void shouldReturnDoneWhenStatusIsDone() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.DONE)
                    .dueDatetime(LocalDateTime.now().minusDays(1)) // Even if past due date
                    .doneDatetime(LocalDateTime.now())
                    .build();

            assertThat(item.getEffectiveStatus()).isEqualTo(TodoStatus.DONE);
        }

        @Test
        @DisplayName("Should return PAST_DUE when status is already PAST_DUE")
        void shouldReturnPastDueWhenStatusIsAlreadyPastDue() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.PAST_DUE)
                    .dueDatetime(LocalDateTime.now().minusDays(1))
                    .build();

            assertThat(item.getEffectiveStatus()).isEqualTo(TodoStatus.PAST_DUE);
        }

        @Test
        @DisplayName("Should return NOT_DONE when due datetime is null")
        void shouldReturnNotDoneWhenDueDatetimeIsNull() {
            TodoItem item = TodoItem.builder()
                    .status(TodoStatus.NOT_DONE)
                    .dueDatetime(null)
                    .build();

            assertThat(item.getEffectiveStatus()).isEqualTo(TodoStatus.NOT_DONE);
        }
    }

    @Nested
    @DisplayName("onCreate() Lifecycle Tests")
    class OnCreateLifecycleTests {

        @Test
        @DisplayName("Should set creation datetime on persist")
        void shouldSetCreationDatetimeOnPersist() {
            TodoItem item = TodoItem.builder()
                    .description("Test")
                    .dueDatetime(LocalDateTime.now().plusDays(1))
                    .build();

            assertThat(item.getCreationDatetime()).isNull();

            item.onCreate();

            assertThat(item.getCreationDatetime()).isNotNull();
        }

        @Test
        @DisplayName("Should set default status to NOT_DONE on persist")
        void shouldSetDefaultStatusOnPersist() {
            TodoItem item = TodoItem.builder()
                    .description("Test")
                    .dueDatetime(LocalDateTime.now().plusDays(1))
                    .build();

            assertThat(item.getStatus()).isNull();

            item.onCreate();

            assertThat(item.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        }

        @Test
        @DisplayName("Should not override existing creation datetime")
        void shouldNotOverrideExistingCreationDatetime() {
            LocalDateTime existingDatetime = LocalDateTime.now().minusDays(5);
            TodoItem item = TodoItem.builder()
                    .description("Test")
                    .dueDatetime(LocalDateTime.now().plusDays(1))
                    .creationDatetime(existingDatetime)
                    .build();

            item.onCreate();

            assertThat(item.getCreationDatetime()).isEqualTo(existingDatetime);
        }

        @Test
        @DisplayName("Should not override existing status")
        void shouldNotOverrideExistingStatus() {
            TodoItem item = TodoItem.builder()
                    .description("Test")
                    .dueDatetime(LocalDateTime.now().plusDays(1))
                    .status(TodoStatus.DONE)
                    .build();

            item.onCreate();

            assertThat(item.getStatus()).isEqualTo(TodoStatus.DONE);
        }
    }
}
