package com.zactonics.apitemplates.controller;

import com.zactonics.apitemplates.service.ProcessingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.concurrent.CompletableFuture;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskControllerTest {

  @Autowired
  private MockMvc mockMvc;

  // ✅ Replace @MockBean with @MockitoBean
  @MockitoBean
  private ProcessingService processingService;

  @Test
  @DisplayName("POST /api/tasks returns 202, sets Location header, and calls service")
  void startTask_acceptsAndReturnsLocation() throws Exception {
    Mockito.when(processingService.performLongRunningTask(eq("hello")))
        .thenReturn(CompletableFuture.completedFuture("OK"));

    MvcResult result = mockMvc.perform(
            post("/api/tasks").contentType(MediaType.TEXT_PLAIN).content("hello"))
        .andExpect(status().isAccepted())
        .andExpect(header().string("Location", startsWith("/api/tasks/")))
        .andReturn();

    Mockito.verify(processingService).performLongRunningTask("hello");

    String body = result.getResponse().getContentAsString();
    assertThat(body.contains("Task accepted:"), is(true));
    // String taskId = body.substring("Task accepted: ".length()).trim();
   // assertThat(taskId, matchesPattern("^[0-9a-fA-F\\-]{36}$"));
   // assertThat(result.getResponse().getHeader("Location"), equalTo("/api/tasks/" + taskId));
  }

  @Test
  void getTaskStatus_returnsOk() throws Exception {
    mockMvc.perform(get("/abc123").accept(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.taskId").value("abc123"))
        .andExpect(jsonPath("$.status").value("abc123"));
  }
}
