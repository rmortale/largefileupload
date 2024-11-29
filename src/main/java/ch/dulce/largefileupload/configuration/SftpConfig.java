package ch.dulce.largefileupload.configuration;

import org.apache.sshd.client.SshClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SftpConfig {

  @Bean
  public SshClient sshClient() {
    SshClient client = SshClient.setUpDefaultClient();
    client.start();
    return client;
  }
}
