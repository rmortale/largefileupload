package ch.dulce.largefileupload.configuration;

import ch.dulce.largefileupload.service.upload.CustomAsyncExceptionHandler;
import java.util.concurrent.Executor;
import lombok.RequiredArgsConstructor;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class CustomAsyncConfig implements AsyncConfigurer {

  private final CustomAsyncExceptionHandler customAsyncExceptionHandler;

  @Override
  public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
    return customAsyncExceptionHandler;
  }

  @Override
  public Executor getAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(3);
    executor.setMaxPoolSize(3);
    executor.setQueueCapacity(500);
    executor.setThreadNamePrefix("ScpUploader-");
    executor.initialize();
    return executor;
  }
}
