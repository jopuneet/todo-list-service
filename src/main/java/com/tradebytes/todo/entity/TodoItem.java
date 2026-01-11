package com.tradebytes.todo.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * Entity representing a Todo item in the database.
 */
@Entity
@Table(name = "todos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TodoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TodoStatus status;

    @Column(name = "creation_datetime", nullable = false, updatable = false)
    private LocalDateTime creationDatetime;

    @Column(name = "due_datetime", nullable = false)
    private LocalDateTime dueDatetime;

    @Column(name = "done_datetime")
    private LocalDateTime doneDatetime;
}
