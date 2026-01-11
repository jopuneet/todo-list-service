package com.tradebytes.todo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.entity.TodoItem;
import com.tradebytes.todo.entity.TodoStatus;
import com.tradebytes.todo.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class TodoIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TodoRepository todoRepository;

    @BeforeEach
    void setUp() {
        todoRepository.deleteAll();
    }

    @Test
    @DisplayName("Full lifecycle: create, update, mark done, mark not done")
    void fullLifecycleTest() throws Exception {
        // Create
        CreateTodoRequest createRequest = CreateTodoRequest.builder()
                .description("Integration test task")
                .dueDatetime(LocalDateTime.now().plusDays(1))
                .build();

        String createResponse = mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.description").value("Integration test task"))
                .andExpect(jsonPath("$.status").value("not done"))
                .andReturn().getResponse().getContentAsString();

        Long todoId = objectMapper.readTree(createResponse).get("id").asLong();

        // Update description
        UpdateDescriptionRequest updateRequest = UpdateDescriptionRequest.builder()
                .description("Updated integration test task")
                .build();

        mockMvc.perform(patch("/api/todos/" + todoId + "/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated integration test task"));

        // Mark as done
        mockMvc.perform(patch("/api/todos/" + todoId + "/done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("done"))
                .andExpect(jsonPath("$.done_datetime").isNotEmpty());

        // Mark as not done
        mockMvc.perform(patch("/api/todos/" + todoId + "/not-done"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("not done"))
                .andExpect(jsonPath("$.done_datetime").isEmpty());
    }

    @Test
    @DisplayName("Should prevent modifications to past due items")
    void shouldPreventModificationsToPastDueItems() throws Exception {
        // Create a past due item directly in the database
        TodoItem pastDueItem = TodoItem.builder()
                .description("Past due task")
                .status(TodoStatus.PAST_DUE)
                .creationDatetime(LocalDateTime.now().minusDays(2))
                .dueDatetime(LocalDateTime.now().minusDays(1))
                .build();
        pastDueItem = todoRepository.save(pastDueItem);

        // Try to update description
        UpdateDescriptionRequest updateRequest = UpdateDescriptionRequest.builder()
                .description("Updated description")
                .build();

        mockMvc.perform(patch("/api/todos/" + pastDueItem.getId() + "/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isConflict());

        // Try to mark as done
        mockMvc.perform(patch("/api/todos/" + pastDueItem.getId() + "/done"))
                .andExpect(status().isConflict());

        // Try to mark as not done
        mockMvc.perform(patch("/api/todos/" + pastDueItem.getId() + "/not-done"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should filter todos by status")
    void shouldFilterTodosByStatus() throws Exception {
        // Create multiple todos with different statuses
        TodoItem notDoneItem = TodoItem.builder()
                .description("Not done task")
                .status(TodoStatus.NOT_DONE)
                .creationDatetime(LocalDateTime.now())
                .dueDatetime(LocalDateTime.now().plusDays(1))
                .build();
        todoRepository.save(notDoneItem);

        TodoItem doneItem = TodoItem.builder()
                .description("Done task")
                .status(TodoStatus.DONE)
                .creationDatetime(LocalDateTime.now())
                .dueDatetime(LocalDateTime.now().plusDays(1))
                .doneDatetime(LocalDateTime.now())
                .build();
        todoRepository.save(doneItem);

        // Get only not done items (default)
        mockMvc.perform(get("/api/todos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].status").value("not done"));

        // Get all items
        mockMvc.perform(get("/api/todos").param("all", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    @DisplayName("Should return 404 for non-existent todo")
    void shouldReturn404ForNonExistentTodo() throws Exception {
        mockMvc.perform(get("/api/todos/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Todo item not found with id: 999"));
    }

    @Test
    @DisplayName("Should validate request body")
    void shouldValidateRequestBody() throws Exception {
        CreateTodoRequest invalidRequest = CreateTodoRequest.builder()
                .description("")
                .dueDatetime(null)
                .build();

        mockMvc.perform(post("/api/todos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").isArray());
    }
}
