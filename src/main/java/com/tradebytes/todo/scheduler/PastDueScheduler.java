package com.tradebytes.todo.scheduler;

import com.tradebytes.todo.service.TodoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Scheduler component for automatic past due status updates.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class PastDueScheduler {

    private final TodoService todoService;

    /**
     * Scheduled task that runs every minute to update past due items.
     * Checks all "not done" items and marks them as "past due" if their
     * due_datetime is in the past.
     */
    @Scheduled(fixedRate = 60000) // Run every minute
    public void updatePastDueItems() {
        log.info("Running scheduled past due check");
        todoService.updatePastDueItems();
    }
}
