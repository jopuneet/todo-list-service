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

    @PrePersist
    protected void onCreate() {
        if (creationDatetime == null) {
            creationDatetime = LocalDateTime.now();
        }
        if (status == null) {
            status = TodoStatus.NOT_DONE;
        }
    }

    /**
     * Check if this item is effectively past due.
     * An item is effectively past due if:
     * - It is already marked as PAST_DUE, OR
     * - It is NOT_DONE and the due date has passed
     * <p>
     * This is the single source of truth for past due determination.
     */
    public boolean isEffectivelyPastDue() {
        if (status == TodoStatus.PAST_DUE) {
            return true;
        }
        return status == TodoStatus.NOT_DONE
                && dueDatetime != null
                && dueDatetime.isBefore(LocalDateTime.now());
    }

    /**
     * Get the effective status for display purposes.
     * Returns PAST_DUE if the item is effectively past due, otherwise the actual status.
     */
    public TodoStatus getEffectiveStatus() {
        return isEffectivelyPastDue() ? TodoStatus.PAST_DUE : status;
    }
}
