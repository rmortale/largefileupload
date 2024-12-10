package ch.dulce.largefileupload.service;

import static ch.dulce.largefileupload.service.FileService.*;

import ch.dulce.largefileupload.repository.FileEntity;
import ch.dulce.largefileupload.repository.FileRepository;
import ch.dulce.largefileupload.service.sftp.SftpGateway;
import java.io.File;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledFileUploaderService {

  @Value("${app.fileUploadBatchSize}")
  private int fileUploadBatchSize;

  @Value("${app.fileDir}")
  private String fileDir;

  private final FileRepository fileRepository;
  private final SftpGateway sftpGateway;
  private static final long initialRetryDelayMinutes = 1;

  @Scheduled(fixedRate = 10007, initialDelay = 20000)
  @Transactional
  public void uploadFile() {
    List<FileEntity> received =
        fileRepository.findByStatus(
            RECEIVED, Sort.by(Sort.Direction.ASC, "receivedAt"), Limit.of(fileUploadBatchSize));
    log.info("Processing {} files.", received.size());

    for (FileEntity file : received) {
      try {
        log.info("Uploading file: {}", file.getOriginalFilename());

        // do the upload
        sftpGateway.sendToSftp(new File(fileDir, file.getSavedFilename()));
        // Thread.sleep(3000);

        if (file.getSourceSystem().equalsIgnoreCase("ex")) {
          throw new RuntimeException("test - could not save file");
        }

        file.setDeliveredAt(LocalDateTime.now());
        file.setStatus(DELIVERED);

        log.info(
            "File {} delivered to {} successfully.",
            file.getOriginalFilename(),
            file.getTargetConnectionName());
      } catch (Exception e) {
        log.error("Error while uploading file", e);
        file.setDeliveredAt(null);
        file.setStatus(RETRYING);
        file.setDeliveryRetriedNum(0);
        file.setNextRetryTime(LocalDateTime.now().plusMinutes(initialRetryDelayMinutes));
        file.setErrorMsg(e.getMessage());
      }
    }
  }
}
