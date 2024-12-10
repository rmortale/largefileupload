package ch.dulce.largefileupload.configuration;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.sftp.outbound.SftpMessageHandler;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;

@Configuration
public class SftpConfig {

  public static final String SFTP_CHANNEL = "sftpChannel";

  @Bean
  public SessionFactory<SftpClient.DirEntry> sessionFactory() {
    DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory(true);
    sessionFactory.setHost("192.168.1.207");
    sessionFactory.setPort(22);
    sessionFactory.setUser("nino");
    sessionFactory.setTimeout(5000);
    return new CachingSessionFactory<>(sessionFactory);
  }

  @Bean
  @ServiceActivator(inputChannel = SFTP_CHANNEL)
  public MessageHandler handler(SessionFactory<SftpClient.DirEntry> sessionFactory) {
    SftpMessageHandler handler = new SftpMessageHandler(sessionFactory);
    handler.setRemoteDirectoryExpressionString("headers['remote-target-dir']");
    handler.setFileNameGenerator(
        new FileNameGenerator() {

          @Override
          public String generateFileName(Message<?> message) {
            return "handlerContent.test";
          }
        });
    return handler;
  }
  //  @Bean
  //  public IntegrationFlow sftpOutboundFlow(CachingSessionFactory cachingSessionFactory) {
  //    return IntegrationFlow.from(SFTP_CHANNEL)
  //        .handle(
  //            Sftp.outboundAdapter(cachingSessionFactory, FileExistsMode.FAIL)
  //                .useTemporaryFileName(false)
  //                .remoteDirectory("/foo"))
  //        .get();
  //  }
}
