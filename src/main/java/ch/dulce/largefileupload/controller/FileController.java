package ch.dulce.largefileupload.controller;

import ch.dulce.largefileupload.event.FileUploadedEvent;
import ch.dulce.largefileupload.repository.FileEntity;
import ch.dulce.largefileupload.repository.FileRepository;
import ch.dulce.largefileupload.repository.OutboxEntity;
import ch.dulce.largefileupload.repository.OutboxRepository;
import ch.dulce.largefileupload.service.upload.ScpUploader;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/")
@Slf4j
public class FileController {

  private static final String SOURCE_SYSTEM = "sourceSystem";
  private static final String SOURCE_ENVIRONMENT = "sourceEnvironment";
  private final String fileDir;
  private final FileRepository fileRepository;
  private final ScpUploader scpUploader;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  public FileController(
      @Value("${app.fileDir}") String fileDir,
      FileRepository fileRepository,
      ScpUploader scpUploader,
      OutboxRepository outboxRepository,
      ObjectMapper objectMapper) {
    this.fileDir = fileDir;
    this.fileRepository = fileRepository;
    this.scpUploader = scpUploader;
    this.outboxRepository = outboxRepository;
    this.objectMapper = objectMapper;
  }

  @Transactional
  @PostMapping(path = "upload")
  public ResponseEntity<Void> upload(
      @RequestHeader(value = SOURCE_SYSTEM, required = false) String srcSystem,
      @RequestHeader(value = SOURCE_ENVIRONMENT, required = false) String srcEnv,
      HttpServletRequest request)
      throws IOException {

    if (srcSystem == null) {
      throw new BadRequestException("Http header sourceSystem is required!");
    }
    if (srcEnv == null) {
      throw new BadRequestException("Http header sourceEnvironment is required!");
    }
    if (!JakartaServletFileUpload.isMultipartContent(request)) {
      throw new BadRequestException("Multipart request expected");
    }
    log.info("Received upload request from system {}, environment {}", srcSystem, srcEnv);

    JakartaServletFileUpload fileUpload = new JakartaServletFileUpload();

    AtomicBoolean fileFound = new AtomicBoolean(false);
    fileUpload
        .getItemIterator(request)
        .forEachRemaining(
            item -> {
              if (!item.isFormField()) {
                fileFound.set(true);
                UUID id = UUID.randomUUID();
                Path filePath = Path.of(fileDir, id.toString());
                long copied =
                    Files.copy(
                        item.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                String md5 = getMd5FromFile(filePath);
                LocalDateTime uploadTime = LocalDateTime.now();
                fileRepository.save(
                    new FileEntity(
                        id,
                        srcSystem,
                        srcEnv,
                        item.getName(),
                        item.getContentType(),
                        md5,
                        copied,
                        uploadTime));
                FileUploadedEvent event =
                    new FileUploadedEvent(
                        UUID.randomUUID(),
                        item.getName(),
                        copied,
                        item.getContentType(),
                        id.toString(),
                        srcEnv,
                        srcSystem,
                        uploadTime,
                        md5);
                outboxRepository.save(
                    new OutboxEntity(
                        UUID.randomUUID(),
                        "FileUploadedEvent",
                        id.toString(),
                        "event",
                        objectMapper.writeValueAsString(event)));

                // TODO: delete outbox record immediately

                // TODO: send file to target
                scpUploader.upload("nino", "elited.local", 22, filePath, "uploaded");
                log.info(
                    "Successfully saved file {} to local storage with size {} bytes and checksum {}",
                    item.getName(),
                    copied,
                    md5);
              }
            });

    if (!fileFound.get()) {
      throw new BadRequestException("No file submitted!");
    }
    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  private String getMd5FromFile(Path filePath) throws IOException {
    try (InputStream inputStream = Files.newInputStream(filePath)) {
      return DigestUtils.md5DigestAsHex(inputStream);
    }
  }
}
