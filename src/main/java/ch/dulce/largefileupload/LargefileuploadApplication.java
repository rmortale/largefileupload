package ch.dulce.largefileupload;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class LargefileuploadApplication {

  public static void main(String[] args) {
    SpringApplication.run(LargefileuploadApplication.class, args);
  }
}
