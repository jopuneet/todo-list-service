package com.tradebytes.todo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.dto.UpdateStatusRequest;
import com.tradebytes.todo.exception.TodoImmutableException;
import com.tradebytes.todo.exception.TodoNotFoundException;
import com.tradebytes.todo.service.TodoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TodoController.class)
class TodoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TodoService todoService;

    private TodoResponse sampleResponse;
    private LocalDateTime now;
    private LocalDateTime futureDate;

    @BeforeEach
    void setUp() {
        now = LocalDateTime.now();
        futureDate = now.plusDays(1);

        sampleResponse = TodoResponse.builder()
                .id(1L)
                .description("Test task")
                .status("not done")
                .creationDatetime(now)
                .dueDatetime(futureDate)
                .build();
    }

    @Nested
    @DisplayName("POST /api/todos - Create Todo")
    class CreateTodoEndpointTests {

        @Test
        @DisplayName("Should create todo successfully")
        void shouldCreateTodoSuccessfully() throws Exception {
            CreateTodoRequest request = CreateTodoRequest.builder()
                    .description("Test task")
                    .dueDatetime(futureDate)
                    .build();

            when(todoService.createTodo(any(CreateTodoRequest.class))).thenReturn(sampleResponse);

            mockMvc.perform(post("/api/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.description").value("Test task"))
                    .andExpect(jsonPath("$.status").value("not done"));
        }

        @Test
        @DisplayName("Should return 400 when description is blank")
        void shouldReturn400WhenDescriptionBlank() throws Exception {
            CreateTodoRequest request = CreateTodoRequest.builder()
                    .description("")
                    .dueDatetime(futureDate)
                    .build();

            mockMvc.perform(post("/api/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when due datetime is null")
        void shouldReturn400WhenDueDatetimeNull() throws Exception {
            CreateTodoRequest request = CreateTodoRequest.builder()
                    .description("Test task")
                    .dueDatetime(null)
                    .build();

            mockMvc.perform(post("/api/todos")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/todos/{id} - Get Todo By ID")
    class GetTodoByIdEndpointTests {

        @Test
        @DisplayName("Should get todo by ID successfully")
        void shouldGetTodoByIdSuccessfully() throws Exception {
            when(todoService.getTodoById(1L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/todos/1"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.description").value("Test task"));
        }

        @Test
        @DisplayName("Should return 404 when todo not found")
        void shouldReturn404WhenTodoNotFound() throws Exception {
            when(todoService.getTodoById(999L)).thenThrow(new TodoNotFoundException(999L));

            mockMvc.perform(get("/api/todos/999"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Todo item not found with id: 999"));
        }
    }

    @Nested
    @DisplayName("GET /api/todos - Get All Todos")
    class GetAllTodosEndpointTests {

        @Test
        @DisplayName("Should get all not done todos by default")
        void shouldGetAllNotDoneTodosByDefault() throws Exception {
            when(todoService.getAllTodos(false)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/todos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(1));
        }

        @Test
        @DisplayName("Should get all todos when all=true")
        void shouldGetAllTodosWhenAllTrue() throws Exception {
            when(todoService.getAllTodos(true)).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/todos").param("all", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray());
        }
    }

    @Nested
    @DisplayName("PATCH /api/todos/{id}/description - Update Description")
    class UpdateDescriptionEndpointTests {

        @Test
        @DisplayName("Should update description successfully")
        void shouldUpdateDescriptionSuccessfully() throws Exception {
            UpdateDescriptionRequest request = UpdateDescriptionRequest.builder()
                    .description("Updated task")
                    .build();

            TodoResponse updatedResponse = TodoResponse.builder()
                    .id(1L)
                    .description("Updated task")
                    .status("not done")
                    .build();

            when(todoService.updateDescription(eq(1L), any(UpdateDescriptionRequest.class)))
                    .thenReturn(updatedResponse);

            mockMvc.perform(patch("/api/todos/1/description")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.description").value("Updated task"));
        }

        @Test
        @DisplayName("Should return 409 when updating past due item")
        void shouldReturn409WhenUpdatingPastDueItem() throws Exception {
            UpdateDescriptionRequest request = UpdateDescriptionRequest.builder()
                    .description("Updated task")
                    .build();

            when(todoService.updateDescription(eq(1L), any(UpdateDescriptionRequest.class)))
                    .thenThrow(new TodoImmutableException(1L));

            mockMvc.perform(patch("/api/todos/1/description")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }
    }

    @Nested
    @DisplayName("PATCH /api/todos/{id}/status - Update Status")
    class UpdateStatusEndpointTests {

        @Test
        @DisplayName("Should update status to done successfully")
        void shouldUpdateStatusToDoneSuccessfully() throws Exception {
            UpdateStatusRequest request = UpdateStatusRequest.builder()
                    .status("done")
                    .build();

            TodoResponse doneResponse = TodoResponse.builder()
                    .id(1L)
                    .description("Test task")
                    .status("done")
                    .doneDatetime(LocalDateTime.now())
                    .build();

            when(todoService.updateStatus(eq(1L), any(UpdateStatusRequest.class)))
                    .thenReturn(doneResponse);

            mockMvc.perform(patch("/api/todos/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("done"));
        }

        @Test
        @DisplayName("Should update status to not done successfully")
        void shouldUpdateStatusToNotDoneSuccessfully() throws Exception {
            UpdateStatusRequest request = UpdateStatusRequest.builder()
                    .status("not done")
                    .build();

            when(todoService.updateStatus(eq(1L), any(UpdateStatusRequest.class)))
                    .thenReturn(sampleResponse);

            mockMvc.perform(patch("/api/todos/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("not done"));
        }

        @Test
        @DisplayName("Should return 409 when updating past due item status")
        void shouldReturn409WhenUpdatingPastDueItemStatus() throws Exception {
            UpdateStatusRequest request = UpdateStatusRequest.builder()
                    .status("done")
                    .build();

            when(todoService.updateStatus(eq(1L), any(UpdateStatusRequest.class)))
                    .thenThrow(new TodoImmutableException(1L));

            mockMvc.perform(patch("/api/todos/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        @DisplayName("Should return 400 when status is invalid")
        void shouldReturn400WhenStatusIsInvalid() throws Exception {
            // Using raw JSON to bypass DTO validation and test controller validation
            String invalidRequest = "{\"status\": \"invalid\"}";

            mockMvc.perform(patch("/api/todos/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(invalidRequest))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 when status is blank")
        void shouldReturn400WhenStatusIsBlank() throws Exception {
            UpdateStatusRequest request = UpdateStatusRequest.builder()
                    .status("")
                    .build();

            mockMvc.perform(patch("/api/todos/1/status")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}
