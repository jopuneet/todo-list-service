package com.tradebytes.todo.integration;

import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import com.tradebytes.todo.repository.TodoRepository;
import com.tradebytes.todo.scheduler.PastDueScheduler;
import com.tradebytes.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for the scheduler functionality.
 * Tests the automatic "past due" status transition mechanism.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SchedulerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TodoRepository todoRepository;

    @Autowired
    private TodoService todoService;

    @Autowired
    private PastDueScheduler pastDueScheduler;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
    }

    @Nested
    @DisplayName("Scheduler Past Due Update Tests")
    class SchedulerPastDueUpdateTests {

        @Test
        @DisplayName("Scheduler should mark overdue items as past due")
        void schedulerShouldMarkOverdueItemsAsPastDue() {
            // Create items with due dates in the past (should become past due)
            TodoItem overdueItem1 = TodoItem.builder()
                    .description("Overdue task 1")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(3))
                    .dueDatetime(LocalDateTime.now().minusHours(1)) // Due 1 hour ago
                    .build();
            todoRepository.save(overdueItem1);

            TodoItem overdueItem2 = TodoItem.builder()
                    .description("Overdue task 2")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(2))
                    .dueDatetime(LocalDateTime.now().minusMinutes(30)) // Due 30 minutes ago
                    .build();
            todoRepository.save(overdueItem2);

            // Create an item that is NOT overdue (should remain NOT_DONE)
            TodoItem futureItem = TodoItem.builder()
                    .description("Future task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now())
                    .dueDatetime(LocalDateTime.now().plusDays(1)) // Due tomorrow
                    .build();
            todoRepository.save(futureItem);

            // Create a done item (should remain DONE regardless of due date)
            TodoItem doneItem = TodoItem.builder()
                    .description("Done task")
                    .status(TodoStatus.DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(2))
                    .dueDatetime(LocalDateTime.now().minusHours(2)) // Due 2 hours ago but already done
                    .doneDatetime(LocalDateTime.now().minusHours(3))
                    .build();
            todoRepository.save(doneItem);

            // Manually trigger the scheduler
            pastDueScheduler.updatePastDueItems();

            // Verify the results
            List<TodoItem> allItems = todoRepository.findAll();
            assertThat(allItems).hasSize(4);

            // Check overdue items are now PAST_DUE
            TodoItem updatedOverdue1 = todoRepository.findById(overdueItem1.getId()).orElseThrow();
            assertThat(updatedOverdue1.getStatus()).isEqualTo(TodoStatus.PAST_DUE);

            TodoItem updatedOverdue2 = todoRepository.findById(overdueItem2.getId()).orElseThrow();
            assertThat(updatedOverdue2.getStatus()).isEqualTo(TodoStatus.PAST_DUE);

            // Check future item is still NOT_DONE
            TodoItem updatedFutureItem = todoRepository.findById(futureItem.getId()).orElseThrow();
            assertThat(updatedFutureItem.getStatus()).isEqualTo(TodoStatus.NOT_DONE);

            // Check done item is still DONE
            TodoItem updatedDoneItem = todoRepository.findById(doneItem.getId()).orElseThrow();
            assertThat(updatedDoneItem.getStatus()).isEqualTo(TodoStatus.DONE);
        }

        @Test
        @DisplayName("Scheduler should not affect already past due items")
        void schedulerShouldNotAffectAlreadyPastDueItems() {
            // Create an item already marked as past due
            TodoItem alreadyPastDue = TodoItem.builder()
                    .description("Already past due task")
                    .status(TodoStatus.PAST_DUE)
                    .creationDatetime(LocalDateTime.now().minusDays(5))
                    .dueDatetime(LocalDateTime.now().minusDays(3))
                    .build();
            todoRepository.save(alreadyPastDue);

            // Trigger the scheduler
            pastDueScheduler.updatePastDueItems();

            // Verify it's still past due
            TodoItem updated = todoRepository.findById(alreadyPastDue.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(TodoStatus.PAST_DUE);
        }

        @Test
        @DisplayName("Scheduler should handle empty repository gracefully")
        void schedulerShouldHandleEmptyRepository() {
            // Ensure repository is empty
            assertThat(todoRepository.count()).isZero();

            // Trigger the scheduler - should not throw any exception
            pastDueScheduler.updatePastDueItems();

            // Still empty
            assertThat(todoRepository.count()).isZero();
        }

        @Test
        @DisplayName("Multiple scheduler runs should be idempotent")
        void multipleSchedulerRunsShouldBeIdempotent() {
            // Create an overdue item
            TodoItem overdueItem = TodoItem.builder()
                    .description("Overdue task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(2))
                    .dueDatetime(LocalDateTime.now().minusHours(1))
                    .build();
            todoRepository.save(overdueItem);

            // Run scheduler multiple times
            pastDueScheduler.updatePastDueItems();
            pastDueScheduler.updatePastDueItems();
            pastDueScheduler.updatePastDueItems();

            // Verify item is past due
            TodoItem updated = todoRepository.findById(overdueItem.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(TodoStatus.PAST_DUE);

            // Only one item exists
            assertThat(todoRepository.count()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Read Operations Are Pure (No Side Effects)")
    class ReadOperationsPurityTests {

        @Test
        @DisplayName("Getting an overdue item should show past due status but NOT modify database")
        void gettingOverdueItemShouldShowPastDueWithoutModifyingDb() throws Exception {
            // Create an overdue item (NOT_DONE but due date has passed)
            TodoItem overdueItem = TodoItem.builder()
                    .description("Overdue task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(2))
                    .dueDatetime(LocalDateTime.now().minusMinutes(5))
                    .build();
            overdueItem = todoRepository.save(overdueItem);

            // Get the item via API - should show "past due" in response
            mockMvc.perform(get("/api/todos/" + overdueItem.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("past due"));

            // But database should remain unchanged (still NOT_DONE)
            TodoItem unchanged = todoRepository.findById(overdueItem.getId()).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        }

        @Test
        @DisplayName("Listing todos should show computed status but NOT modify database")
        void listingTodosShouldShowComputedStatusWithoutModifyingDb() throws Exception {
            // Create an overdue item
            TodoItem overdueItem = TodoItem.builder()
                    .description("Overdue task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(1))
                    .dueDatetime(LocalDateTime.now().minusMinutes(10))
                    .build();
            todoRepository.save(overdueItem);

            // List all todos - should show "past due" in response
            mockMvc.perform(get("/api/todos").param("all", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.length()").value(1))
                    .andExpect(jsonPath("$[0].status").value("past due"));

            // But database should remain unchanged
            TodoItem unchanged = todoRepository.findById(overdueItem.getId()).orElseThrow();
            assertThat(unchanged.getStatus()).isEqualTo(TodoStatus.NOT_DONE);
        }

        @Test
        @DisplayName("Scheduler should persist the past due status to database")
        void schedulerShouldPersistPastDueStatus() throws Exception {
            // Create an overdue item
            TodoItem overdueItem = TodoItem.builder()
                    .description("Overdue task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(1))
                    .dueDatetime(LocalDateTime.now().minusMinutes(5))
                    .build();
            overdueItem = todoRepository.save(overdueItem);

            // Before scheduler: DB has NOT_DONE, API shows "past due"
            TodoItem beforeScheduler = todoRepository.findById(overdueItem.getId()).orElseThrow();
            assertThat(beforeScheduler.getStatus()).isEqualTo(TodoStatus.NOT_DONE);

            mockMvc.perform(get("/api/todos/" + overdueItem.getId()))
                    .andExpect(jsonPath("$.status").value("past due"));

            // Run scheduler - NOW database should be updated
            pastDueScheduler.updatePastDueItems();

            // After scheduler: DB also has PAST_DUE
            TodoItem afterScheduler = todoRepository.findById(overdueItem.getId()).orElseThrow();
            assertThat(afterScheduler.getStatus()).isEqualTo(TodoStatus.PAST_DUE);
        }
    }

    @Nested
    @DisplayName("Overdue Item Modification Prevention Tests")
    class OverdueItemModificationPreventionTests {

        @Test
        @DisplayName("Attempting to modify an overdue item should fail with conflict")
        void attemptingToModifyOverdueItemShouldFail() throws Exception {
            // Create an overdue item that hasn't been marked as past due yet
            TodoItem overdueItem = TodoItem.builder()
                    .description("Overdue task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(1))
                    .dueDatetime(LocalDateTime.now().minusMinutes(5))
                    .build();
            overdueItem = todoRepository.save(overdueItem);

            // Try to update status - should fail because the item is overdue
            String statusRequest = "{\"status\": \"done\"}";

            mockMvc.perform(patch("/api/todos/" + overdueItem.getId() + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusRequest))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Attempting to update description of overdue item should fail")
        void attemptingToUpdateDescriptionOfOverdueItemShouldFail() throws Exception {
            // Create an overdue item
            TodoItem overdueItem = TodoItem.builder()
                    .description("Overdue task")
                    .status(TodoStatus.NOT_DONE)
                    .creationDatetime(LocalDateTime.now().minusDays(1))
                    .dueDatetime(LocalDateTime.now().minusMinutes(5))
                    .build();
            overdueItem = todoRepository.save(overdueItem);

            // Try to update description - should fail
            String descRequest = "{\"description\": \"Updated description\"}";

            mockMvc.perform(patch("/api/todos/" + overdueItem.getId() + "/description")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(descRequest))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("Past Due Immutability Tests")
    class PastDueImmutabilityTests {

        @Test
        @DisplayName("Past due items should be completely immutable via API")
        void pastDueItemsShouldBeImmutable() throws Exception {
            // Create a past due item
            TodoItem pastDueItem = TodoItem.builder()
                    .description("Past due task")
                    .status(TodoStatus.PAST_DUE)
                    .creationDatetime(LocalDateTime.now().minusDays(3))
                    .dueDatetime(LocalDateTime.now().minusDays(1))
                    .build();
            pastDueItem = todoRepository.save(pastDueItem);

            // Try to update description - should fail
            String descRequest = "{\"description\": \"Updated description\"}";
            mockMvc.perform(patch("/api/todos/" + pastDueItem.getId() + "/description")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(descRequest))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value(
                            "Cannot modify todo item with id: " + pastDueItem.getId() + ". Item is past due and immutable."));

            // Try to update status to done - should fail
            String statusRequest = "{\"status\": \"done\"}";
            mockMvc.perform(patch("/api/todos/" + pastDueItem.getId() + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(statusRequest))
                    .andExpect(status().isConflict());

            // Try to update status to not done - should fail
            String notDoneRequest = "{\"status\": \"not done\"}";
            mockMvc.perform(patch("/api/todos/" + pastDueItem.getId() + "/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(notDoneRequest))
                    .andExpect(status().isConflict());

            // Verify item is unchanged
            TodoItem unchanged = todoRepository.findById(pastDueItem.getId()).orElseThrow();
            assertThat(unchanged.getDescription()).isEqualTo("Past due task");
            assertThat(unchanged.getStatus()).isEqualTo(TodoStatus.PAST_DUE);
        }

        @Test
        @DisplayName("Past due items can still be read via API")
        void pastDueItemsCanBeRead() throws Exception {
            // Create a past due item
            TodoItem pastDueItem = TodoItem.builder()
                    .description("Past due task")
                    .status(TodoStatus.PAST_DUE)
                    .creationDatetime(LocalDateTime.now().minusDays(3))
                    .dueDatetime(LocalDateTime.now().minusDays(1))
                    .build();
            pastDueItem = todoRepository.save(pastDueItem);

            // Should be able to read the item
            mockMvc.perform(get("/api/todos/" + pastDueItem.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(pastDueItem.getId()))
                    .andExpect(jsonPath("$.description").value("Past due task"))
                    .andExpect(jsonPath("$.status").value("past due"));

            // Should appear in the list with all=true
            mockMvc.perform(get("/api/todos").param("all", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$[0].status").value("past due"));
        }
    }
}
