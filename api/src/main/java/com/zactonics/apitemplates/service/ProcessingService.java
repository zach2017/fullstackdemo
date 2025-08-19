package com.zactonics.apitemplates.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class ProcessingService {

    @Async("taskExecutor")  // Uses the custom executor
    public CompletableFuture<String> performLongRunningTask(String input) {
        // Simulate or implement long-running process, e.g., AI call or OCR
        try {
            Thread.sleep(5000);  // Placeholder for actual work
            // Replace with: String result = aiClient.callModel(input);
            // Or: String text = ocrEngine.extractText(inputImage);
            log.info("Submission Completed");
            return CompletableFuture.completedFuture("Processed: " + input);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return CompletableFuture.failedFuture(e);
        }
    }
}
