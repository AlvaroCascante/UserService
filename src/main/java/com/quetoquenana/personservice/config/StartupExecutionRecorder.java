package com.quetoquenana.personservice.config;

import com.quetoquenana.personservice.service.ExecutionService;
import org.springframework.context.event.EventListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class StartupExecutionRecorder {
    private final ExecutionService executionService;

    /**
     * Save a new execution record when the application is fully started.
     */
    @EventListener(ApplicationReadyEvent.class)
    public void recordExecutionOnStartup() {
        log.info("Application started, recording execution event.");
        //executionService.saveExecutionOnStartup();
    }
}