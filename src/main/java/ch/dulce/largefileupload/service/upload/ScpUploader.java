package ch.dulce.largefileupload.service.upload;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.session.Session;
import org.apache.sshd.scp.client.CloseableScpClient;
import org.apache.sshd.scp.client.ScpClient;
import org.apache.sshd.scp.client.ScpClientCreator;
import org.apache.sshd.scp.common.ScpTransferEventListener;
import org.apache.sshd.scp.common.helpers.ScpAckInfo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScpUploader {

  private final SshClient sshClient;

  @Async
  public void upload(String user, String host, int port, Path localPath, String remotePath) {

    try (CloseableScpClient scpClient = createClosableScpClient(user, host, port)) {
      scpClient.upload(localPath, remotePath, ScpClient.Option.TargetIsDirectory);
    } catch (IOException e) {
      log.error("Failed to upload file {}", e.getMessage(), e);
    }
  }

  private ClientSession createSession(String user, String host, int port) {
    ClientSession session = null;
    try {
      session = sshClient.connect(user, host, port).verify().getSession();
      session.auth().verify();
      return session;
    } catch (IOException e) {
      log.error("Could not connect to host {}. Error {}", host, e.getMessage());
      return null;
    }
  }

  private CloseableScpClient createClosableScpClient(String user, String host, int port) {
    ClientSession session = createSession(user, host, port);
    return CloseableScpClient.singleSessionInstance(
        ScpClientCreator.instance().createScpClient(session, createScpListener()));
  }

  private ScpTransferEventListener createScpListener() {
    return new ScpTransferEventListener() {
      @Override
      public void startFileEvent(
          Session session, FileOperation op, Path file, long length, Set<PosixFilePermission> perms)
          throws IOException {
        log.info("Starting file {}", file);
        ScpTransferEventListener.super.startFileEvent(session, op, file, length, perms);
      }

      @Override
      public void endFileEvent(
          Session session,
          FileOperation op,
          Path file,
          long length,
          Set<PosixFilePermission> perms,
          Throwable thrown)
          throws IOException {
        log.info("Ending file {}", file);
        ScpTransferEventListener.super.endFileEvent(session, op, file, length, perms, thrown);
      }

      @Override
      public void handleFileEventAckInfo(
          Session session,
          FileOperation op,
          Path file,
          long length,
          Set<PosixFilePermission> perms,
          ScpAckInfo ackInfo)
          throws IOException {
        log.info("Handling file ack info {}", ackInfo);
        ScpTransferEventListener.super.handleFileEventAckInfo(
            session, op, file, length, perms, ackInfo);
      }

      @Override
      public void handleReceiveCommandAckInfo(Session session, String command, ScpAckInfo ackInfo)
          throws IOException {
        log.info("Handling receive command ack info {}", ackInfo);
        ScpTransferEventListener.super.handleReceiveCommandAckInfo(session, command, ackInfo);
      }

      @Override
      public void startFolderEvent(
          Session session, FileOperation op, Path file, Set<PosixFilePermission> perms)
          throws IOException {
        log.info("Starting folder {}", file);
        ScpTransferEventListener.super.startFolderEvent(session, op, file, perms);
      }

      @Override
      public void endFolderEvent(
          Session session,
          FileOperation op,
          Path file,
          Set<PosixFilePermission> perms,
          Throwable thrown)
          throws IOException {
        log.info("Ending folder {}", file);
        ScpTransferEventListener.super.endFolderEvent(session, op, file, perms, thrown);
      }
    };
  }
}
