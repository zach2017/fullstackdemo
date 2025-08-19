package com.zactonics.apitemplates.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.zactonics.apitemplates.service.ProcessingService;

import lombok.extern.slf4j.Slf4j;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
public class TaskController {

    private final ProcessingService processingService;

    public TaskController(ProcessingService processingService) {
        this.processingService = processingService;
    }

    @PostMapping("/api/tasks")
    public ResponseEntity<String> startTask(@RequestBody String input) throws InterruptedException, ExecutionException {
        String taskId = UUID.randomUUID().toString();

         log.info("Task: Submitted: " + taskId);
        // Store taskId in a repo for status tracking (e.g., Redis or DB)
        CompletableFuture<String> future = processingService.performLongRunningTask(input);
        // Optionally handle completion: future.thenAccept(result -> saveResult(taskId, result));
         log.info("Task: In Progress: " + taskId);
         log.info("Task: Complete:" + taskId + "-> " + future.get().toString());
        return ResponseEntity.status(HttpStatus.ACCEPTED)
                             .header("Location", "/api/tasks/" + taskId)
                             .body("Task accepted: " + taskId);
    }

   @GetMapping("/{id}")
    public ResponseEntity<TaskStatus> getTaskStatus(@PathVariable String id) {
        log.info("Get Info For Task Id:" +  id);
        // Add Store demp
        TaskStatus status = new TaskStatus(id, id); //taskStore.get(id);
        if (status == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(status);
    }

    static class TaskStatus {
        private final String taskId;
        private final String status;
        private final String result;
        private final String error;

        public TaskStatus(String taskId, String status) {
            this(taskId, status, null, null);
        }

        public TaskStatus(String taskId, String status, String result) {
            this(taskId, status, result, null);
        }

        public TaskStatus(String taskId, String status, String result, String error) {
            this.taskId = taskId;
            this.status = status;
            this.result = result;
            this.error = error;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getStatus() {
            return status;
        }

        public String getResult() {
            return result;
        }

        public String getError() {
            return error;
        }
    }
        
}