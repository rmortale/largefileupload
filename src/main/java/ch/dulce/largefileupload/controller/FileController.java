package ch.dulce.largefileupload.controller;

import ch.dulce.largefileupload.repository.FileEntity;
import ch.dulce.largefileupload.repository.FileRepository;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
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
@RequestMapping("/")
@Slf4j
public class FileController {

  private static final String SOURCE_SYSTEM = "sourceSystem";
  private static final String SOURCE_ENVIRONMENT = "sourceEnvironment";
  private final String fileDir;
  private final FileRepository repository;

  public FileController(@Value("${app.fileDir}") String fileDir, FileRepository repository) {
    this.fileDir = fileDir;
    this.repository = repository;
  }

  @PostMapping("upload")
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
    log.info("Received request from system {}, environment {}", srcSystem, srcEnv);

    JakartaServletFileUpload fileUpload = new JakartaServletFileUpload();

    fileUpload
        .getItemIterator(request)
        .forEachRemaining(
            item -> {
              if (!item.isFormField()) {
                UUID id = UUID.randomUUID();
                Path filePath = Path.of(fileDir, id.toString());
                long copied =
                    Files.copy(
                        item.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
                String md5 = getMd5FromFile(filePath);
                repository.save(
                    new FileEntity(
                        id,
                        srcSystem,
                        srcEnv,
                        item.getName(),
                        item.getContentType(),
                        md5,
                        copied,
                        LocalDateTime.now()));
                log.info(
                    "Saved file {} with size {} bytes and checksum {}",
                    item.getName(),
                    copied,
                    md5);
              }
            });

    return ResponseEntity.status(HttpStatus.ACCEPTED).build();
  }

  private String getMd5FromFile(Path filePath) throws IOException {
    try (InputStream inputStream = Files.newInputStream(filePath)) {
      return DigestUtils.md5DigestAsHex(inputStream);
    }
  }
}
