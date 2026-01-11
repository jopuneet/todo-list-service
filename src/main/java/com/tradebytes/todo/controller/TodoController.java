package com.tradebytes.todo.controller;

import com.tradebytes.todo.dto.CreateTodoRequest;
import com.tradebytes.todo.dto.ErrorResponse;
import com.tradebytes.todo.dto.TodoResponse;
import com.tradebytes.todo.dto.UpdateDescriptionRequest;
import com.tradebytes.todo.service.TodoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Todo operations.
 */
@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
@Tag(name = "Todos", description = "Todo item management operations")
public class TodoController {

    private final TodoService todoService;

    /**
     * Create a new todo item.
     */
    @Operation(
            summary = "Create a new todo item",
            description = "Creates a new todo item with the provided description and due datetime"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Todo item created successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping
    public ResponseEntity<TodoResponse> createTodo(@Valid @RequestBody CreateTodoRequest request) {
        TodoResponse response = todoService.createTodo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Get a todo item by ID.
     */
    @Operation(
            summary = "Get a todo item by ID",
            description = "Retrieves the full details of a single todo item"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Todo item details",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Todo item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<TodoResponse> getTodoById(
            @Parameter(description = "Unique identifier of the todo item", required = true)
            @PathVariable Long id) {
        TodoResponse response = todoService.getTodoById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Get all todo items.
     */
    @Operation(
            summary = "Get all todo items",
            description = "Retrieves all todo items. By default, returns only items with 'not done' status. " +
                    "Use the 'all' query parameter to retrieve all items regardless of status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "List of todo items",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = TodoResponse.class)))
            )
    })
    @GetMapping
    public ResponseEntity<List<TodoResponse>> getAllTodos(
            @Parameter(description = "If true, returns all items regardless of status. If false (default), returns only 'not done' items.")
            @RequestParam(name = "all", defaultValue = "false") boolean includeAll) {
        List<TodoResponse> response = todoService.getAllTodos(includeAll);
        return ResponseEntity.ok(response);
    }

    /**
     * Update the description of a todo item.
     */
    @Operation(
            summary = "Update todo item description",
            description = "Updates the description of an existing todo item. " +
                    "**Note:** This operation is not allowed for items with 'past due' status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Description updated successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request body",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Todo item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot modify past due item",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}/description")
    public ResponseEntity<TodoResponse> updateDescription(
            @Parameter(description = "Unique identifier of the todo item", required = true)
            @PathVariable Long id,
            @Valid @RequestBody UpdateDescriptionRequest request) {
        TodoResponse response = todoService.updateDescription(id, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a todo item as done.
     */
    @Operation(
            summary = "Mark todo item as done",
            description = "Marks a todo item as 'done' and sets the done_datetime to the current timestamp. " +
                    "**Note:** This operation is not allowed for items with 'past due' status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item marked as done successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Todo item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot modify past due item",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}/done")
    public ResponseEntity<TodoResponse> markAsDone(
            @Parameter(description = "Unique identifier of the todo item", required = true)
            @PathVariable Long id) {
        TodoResponse response = todoService.markAsDone(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Mark a todo item as not done.
     */
    @Operation(
            summary = "Mark todo item as not done",
            description = "Marks a todo item as 'not done' and clears the done_datetime. " +
                    "**Note:** This operation is not allowed for items with 'past due' status."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Item marked as not done successfully",
                    content = @Content(schema = @Schema(implementation = TodoResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Todo item not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cannot modify past due item",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PatchMapping("/{id}/not-done")
    public ResponseEntity<TodoResponse> markAsNotDone(
            @Parameter(description = "Unique identifier of the todo item", required = true)
            @PathVariable Long id) {
        TodoResponse response = todoService.markAsNotDone(id);
        return ResponseEntity.ok(response);
    }
}
