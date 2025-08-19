package com.zactonics.apitemplates.service;

import static org.junit.jupiter.api.Assertions.*;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = {
    ProcessingService.class,
    ProcessingServiceTest.AsyncTestConfig.class
})
@ActiveProfiles("test")
class ProcessingServiceTest {

  @Autowired
  private ProcessingService processingService;

  @TestConfiguration
  @EnableAsync
  static class AsyncTestConfig {
    @Bean(name = "taskExecutor")
    public ThreadPoolTaskExecutor taskExecutor() {
      ThreadPoolTaskExecutor ex = new ThreadPoolTaskExecutor();
      ex.setCorePoolSize(1);
      ex.setMaxPoolSize(1);
      ex.setQueueCapacity(10);
      ex.setThreadNamePrefix("async-test-");
      ex.initialize();
      return ex;
    }
  }

  @Test
  @DisplayName("performLongRunningTask runs on the async executor and returns the processed value")
  void performsAsyncAndReturnsResult() throws Exception {
    long start = System.nanoTime();

    CompletableFuture<String> future = processingService.performLongRunningTask("abc");

    // Should not be done immediately (work runs on async thread)
    assertFalse(future.isDone(), "Future should not be done immediately");

    // Capture the completion thread name (should be our named executor)
    String completionThread =
        future.thenApply(v -> Thread.currentThread().getName())
              .get(7, TimeUnit.SECONDS);

    assertTrue(completionThread.startsWith("async-test-"),
        "Expected completion on 'async-test-' thread, got: " + completionThread);

    // Final result
    String result = future.get(7, TimeUnit.SECONDS);
    assertEquals("Processed: abc", result);

    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    assertTrue(elapsedMs >= 4900,
        "Sleep should roughly take ~5s (elapsed=" + elapsedMs + "ms)");
  }

  @Test
  @DisplayName("If the thread is interrupted before sleep, the future completes exceptionally")
  void handlesInterruptedExceptionImmediately() {
    // Direct call (no Spring proxy): set interrupt flag so first sleep throws immediately
    ProcessingService plain = new ProcessingService();
    try {
      Thread.currentThread().interrupt(); // force immediate InterruptedException in sleep()

      CompletableFuture<String> failed = plain.performLongRunningTask("x");
      assertTrue(failed.isCompletedExceptionally(), "Expected a failed future due to interrupt");

      CompletionException ex = assertThrows(CompletionException.class, failed::join);
      assertNotNull(ex.getCause());
      assertEquals(InterruptedException.class, ex.getCause().getClass(),
          "Underlying cause should be InterruptedException");
    } finally {
      // Clear interrupt status to avoid leaking to other tests
      Thread.interrupted();
    }
  }
}
