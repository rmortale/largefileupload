package ch.dulce.largefileupload.service;

import static ch.dulce.largefileupload.service.FileService.*;

import ch.dulce.largefileupload.repository.FileEntity;
import ch.dulce.largefileupload.repository.FileRepository;
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
public class ScheduledFileRetryService {

  @Value("${app.maxUploadRetries}")
  private int maxRetries;

  @Value("${app.fileRetryBatchSize}")
  private int fileRetryBatchSize;

  private final FileRepository fileRepository;

  @Scheduled(fixedRate = 20011, initialDelay = 21000)
  @Transactional
  public void retryFile() {
    List<FileEntity> received =
        fileRepository.findByStatus(
            RETRYING, Sort.by(Sort.Direction.ASC, "nextRetryTime"), Limit.of(fileRetryBatchSize));
    log.info("Retrying {} files.", received.size());

    for (FileEntity file : received) {
      try {
        log.info("Retrying file: {}", file.getOriginalFilename());

        if (file.getNextRetryTime().isAfter(LocalDateTime.now())) {
          log.info("Retrying file: {} at {}", file.getOriginalFilename(), file.getNextRetryTime());
          continue;
        }
        // do the upload
        Thread.sleep(3000);

        if (file.getSourceSystem().equalsIgnoreCase("ex")) {
          throw new RuntimeException("test - could not retry file");
        }

        file.setDeliveredAt(LocalDateTime.now());
        file.setStatus(DELIVERED);

        log.info(
            "File {} uploaded to {} successfully.",
            file.getOriginalFilename(),
            file.getTargetConnectionName());
      } catch (Exception e) {
        log.error("error while retrying file", e);
        file.setDeliveredAt(null);
        file.setStatus(RETRYING);
        file.setDeliveryRetriedNum(file.getDeliveryRetriedNum() + 1);
        file.setErrorMsg(e.getMessage());
        file.setNextRetryTime(LocalDateTime.now().plusMinutes(file.getDeliveryRetriedNum() * 2));
        if (file.getDeliveryRetriedNum() >= maxRetries) {
          log.info(
              "Could not deliver file: {} after {} retries",
              file.getOriginalFilename(),
              maxRetries);
          file.setNextRetryTime(null);
          file.setStatus(NOT_DELIVERED);
        }
      }
    }
  }
}
