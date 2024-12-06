package ch.dulce.largefileupload.service;

import ch.dulce.largefileupload.controller.FileDto;
import ch.dulce.largefileupload.controller.FileResponse;
import ch.dulce.largefileupload.repository.FileEntity;
import ch.dulce.largefileupload.repository.FileRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

@Service
public class FileService {

  public static final String RECEIVED = "received";
  public static final String DELIVERED = "delivered";
  public static final String RETRYING = "retrying";
  public static final String NOT_DELIVERED = "notdelivered";

  private final String fileDir;
  private final FileRepository fileRepository;

  public FileService(@Value("${app.fileDir}") String fileDir, FileRepository fileRepository) {
    this.fileDir = fileDir;
    this.fileRepository = fileRepository;
  }

  public FileResponse saveAndAddFileRecord(FileDto dto) throws IOException {
    Path filePath = Path.of(fileDir, UUID.randomUUID().toString());
    long copied =
        Files.copy(
            dto.getFileItem().getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    FileEntity fileEntity =
        FileEntity.builder()
            .sourceSystem(dto.getSrcSystem())
            .sourceEnvironment(dto.getSrcEnv())
            .targetConnectionName(dto.getTargetConnectionName())
            .savedFilename(filePath.getFileName().toString())
            .contentType(dto.getFileItem().getContentType())
            .originalFilename(dto.getFileItem().getName())
            .deliveryRetriedNum(0)
            .md5Checksum(getMd5FromFile(filePath))
            .sizeBytes(copied)
            .status(RECEIVED)
            .receivedAt(LocalDateTime.now())
            .build();

    FileEntity entity = fileRepository.save(fileEntity);

    return FileResponse.builder()
        .fileTracingId(entity.getId())
        .sizeBytes(entity.getSizeBytes())
        .originalFilename(entity.getOriginalFilename())
        .build();
  }

  private String getMd5FromFile(Path filePath) throws IOException {
    try (InputStream inputStream = Files.newInputStream(filePath)) {
      return DigestUtils.md5DigestAsHex(inputStream);
    }
  }
}
