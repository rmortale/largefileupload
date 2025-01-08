package ch.dulce.largefileupload.configuration;

import org.apache.sshd.sftp.client.SftpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.file.remote.session.CachingSessionFactory;
import org.springframework.integration.file.remote.session.SessionFactory;
import org.springframework.integration.file.support.FileExistsMode;
import org.springframework.integration.sftp.dsl.Sftp;
import org.springframework.integration.sftp.session.DefaultSftpSessionFactory;

@Configuration
public class SftpConfig {

  public static final String SFTP_CHANNEL = "sftpChannel";

  @Bean
  public SessionFactory<SftpClient.DirEntry> sessionFactory() {
    DefaultSftpSessionFactory sessionFactory = new DefaultSftpSessionFactory(true);
    sessionFactory.setHost("172.18.0.1");
    sessionFactory.setPort(2222);
    sessionFactory.setUser("foo");
    sessionFactory.setPassword("pass");
    // sessionFactory.setPrivateKey(new FileSystemResource("/home/nino/.ssh/id_ed25519"));
    sessionFactory.setTimeout(5000);
    sessionFactory.setAllowUnknownKeys(true);
    return new CachingSessionFactory<>(sessionFactory);
  }

  //  @Bean
  //  @ServiceActivator(inputChannel = SFTP_CHANNEL)
  //  public MessageHandler handler(SessionFactory<SftpClient.DirEntry> sessionFactory) {
  //    SftpMessageHandler handler = new SftpMessageHandler(sessionFactory);
  //    handler.setRemoteDirectoryExpressionString("headers['remote-target-dir']");
  //    handler.setFileNameGenerator(
  //        new FileNameGenerator() {
  //
  //          @Override
  //          public String generateFileName(Message<?> message) {
  //            return "handlerContent.test";
  //          }
  //        });
  //    return handler;
  //  }
  @Bean
  public IntegrationFlow sftpOutboundFlow() {
    return IntegrationFlow.from(SFTP_CHANNEL)
        .handle(
            Sftp.outboundAdapter(sessionFactory(), FileExistsMode.FAIL)
                .useTemporaryFileName(false)
                .remoteDirectory("./upload"))
        .get();
  }
}
