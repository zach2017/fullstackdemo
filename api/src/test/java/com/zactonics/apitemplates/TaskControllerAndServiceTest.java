package com.zactonics.apitemplates;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import com.zactonics.apitemplates.service.ProcessingService;

import java.util.concurrent.CompletableFuture;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureMockMvc
public class TaskControllerAndServiceTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProcessingService processingService;

    @Test
     void testGetAllTasks_ReturnsTaskList() throws Exception {
    // Arrange: Simulate two tasks
    mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"input1\""));
    mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
            .contentType(MediaType.APPLICATION_JSON)
            .content("\"input2\""));

    // Act & Assert
    mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks"))
            .andExpect(MockMvcResultMatchers.status().isOk())
            .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
            .andExpect(MockMvcResultMatchers.jsonPath("$.length()").value(2))
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].taskId").exists())
            .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").exists());
}
    @Test
    void testStartTask_ReturnsAcceptedAndTaskId() throws Exception {
        // Arrange
        String input = "test input";
        when(processingService.performLongRunningTask(anyString()))
            .thenReturn(CompletableFuture.completedFuture("Processed: " + input));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + input + "\""))
                .andExpect(MockMvcResultMatchers.status().isAccepted())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.content().string(org.hamcrest.Matchers.containsString("Task accepted:")));
    }

    @Test
    void testGetTaskStatus_ValidId_ReturnsStatus() throws Exception {
        // Arrange
        String taskId = "123e4567-e89b-12d3-a456-426614174000";
        String input = "test input";
        when(processingService.performLongRunningTask(anyString()))
            .thenReturn(CompletableFuture.completedFuture("Processed: " + input));

        // Simulate task creation
        mockMvc.perform(MockMvcRequestBuilders.post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("\"" + input + "\""));

        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks/" + taskId))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.taskId").value(taskId))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").exists());
    }

    @Test
    void testGetTaskStatus_InvalidId_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(MockMvcRequestBuilders.get("/api/tasks/invalid-id"))
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    void testProcessingService_AsyncTaskCompletesSuccessfully() throws Exception {
        // Arrange
        ProcessingService service = new ProcessingService();
        String input = "test input";

        // Act
        CompletableFuture<String> future = service.performLongRunningTask(input);

        // Assert
        String result = future.get(); // Blocks until completion
        assert result.equals("Processed: " + input);
    }

    @Test
    void testProcessingService_AsyncTaskHandlesError() throws Exception {
        // Arrange
        ProcessingService service = new ProcessingService() {
            @Override
            public CompletableFuture<String> performLongRunningTask(String input) {
                return CompletableFuture.supplyAsync(() -> {
                    throw new RuntimeException("Task failed");
                });
            }
        };

        // Act
        CompletableFuture<String> future = service.performLongRunningTask("test input");

        // Assert
        try {
            future.get();
            assert false : "Expected exception";
        } catch (Exception e) {
            assert e.getCause().getMessage().equals("Task failed");
        }
    }
}