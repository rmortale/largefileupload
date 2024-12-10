package ch.dulce.largefileupload.service.sftp;

import static ch.dulce.largefileupload.configuration.SftpConfig.SFTP_CHANNEL;

import java.io.File;
import org.springframework.integration.annotation.Gateway;
import org.springframework.integration.annotation.MessagingGateway;

@MessagingGateway
public interface SftpGateway {

  @Gateway(requestChannel = SFTP_CHANNEL)
  void sendToSftp(final File file);
}
