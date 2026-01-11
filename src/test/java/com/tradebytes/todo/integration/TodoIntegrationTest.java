package com.tradebytes.todo.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.dto.UpdateStatusRequest;
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
    @DisplayName("Full lifecycle: create, update description, update status")
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
        UpdateDescriptionRequest updateDescRequest = UpdateDescriptionRequest.builder()
                .description("Updated integration test task")
                .build();

        mockMvc.perform(patch("/api/todos/" + todoId + "/description")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDescRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Updated integration test task"));

        // Update status to done
        UpdateStatusRequest doneRequest = UpdateStatusRequest.builder()
                .status("done")
                .build();

        mockMvc.perform(patch("/api/todos/" + todoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(doneRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("done"))
                .andExpect(jsonPath("$.done_datetime").isNotEmpty());

        // Update status back to not done
        UpdateStatusRequest notDoneRequest = UpdateStatusRequest.builder()
                .status("not done")
                .build();

        mockMvc.perform(patch("/api/todos/" + todoId + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(notDoneRequest)))
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

        // Try to update status
        UpdateStatusRequest statusRequest = UpdateStatusRequest.builder()
                .status("done")
                .build();

        mockMvc.perform(patch("/api/todos/" + pastDueItem.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(statusRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Should validate status values")
    void shouldValidateStatusValues() throws Exception {
        // Create a todo first
        TodoItem todoItem = TodoItem.builder()
                .description("Test task")
                .status(TodoStatus.NOT_DONE)
                .creationDatetime(LocalDateTime.now())
                .dueDatetime(LocalDateTime.now().plusDays(1))
                .build();
        todoItem = todoRepository.save(todoItem);

        // Try to set invalid status
        String invalidStatusRequest = "{\"status\": \"invalid\"}";

        mockMvc.perform(patch("/api/todos/" + todoItem.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidStatusRequest))
                .andExpect(status().isBadRequest());

        // Try to set past due status via API
        String pastDueRequest = "{\"status\": \"past due\"}";

        mockMvc.perform(patch("/api/todos/" + todoItem.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(pastDueRequest))
                .andExpect(status().isBadRequest());
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
