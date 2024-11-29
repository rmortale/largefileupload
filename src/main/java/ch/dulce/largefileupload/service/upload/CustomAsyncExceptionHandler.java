package ch.dulce.largefileupload.service.upload;

import java.lang.reflect.Method;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CustomAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

  @Override
  public void handleUncaughtException(Throwable ex, Method method, Object... params) {
    System.out.println("Exception Caught in Thread - " + Thread.currentThread().getName());

    System.out.println("Exception message - " + ex.getMessage());

    System.out.println("Method name - " + method.getName());

    for (Object param : params) {

      System.out.println("Parameter value - " + param);
    }
  }
}
