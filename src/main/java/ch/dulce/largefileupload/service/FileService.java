package ch.dulce.largefileupload.service;

import ch.dulce.largefileupload.entity.FileEntity;
import ch.dulce.largefileupload.entity.OutboxEntity;
import ch.dulce.largefileupload.exception.BadRequestException;
import ch.dulce.largefileupload.repository.FileRepository;
import ch.dulce.largefileupload.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.core.FileItemInput;
import org.apache.commons.fileupload2.core.FileItemInputIterator;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

@Service
@Slf4j
@Transactional
@RequiredArgsConstructor
public class FileService {

  private final FileRepository fileRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Value("${app.fileDir}")
  private String fileDir;

  public List<UUID> uploadFiles(
      FileItemInputIterator iter, String srcSystem, String srcEnv, String targetConnectionName)
      throws IOException {
    List<UUID> idList = new ArrayList<>();

    iter.forEachRemaining(
        item -> {
          if (!item.isFormField()) {
            if (!StringUtils.hasText(item.getName())) {
              throw new BadRequestException("Original file name is required!");
            }

            FileEntity fileEntity =
                saveAndAddFileRecord(item, srcSystem, srcEnv, targetConnectionName);
            idList.add(fileEntity.getId());
            OutboxEntity outboxEntity =
                OutboxEntity.builder()
                    .eventPayload(objectMapper.writeValueAsString(fileEntity))
                    .build();
            outboxRepository.save(outboxEntity);
            log.info("Successfully saved file with id: {}.", fileEntity.getId());
          }
        });
    return idList;
  }

  private FileEntity saveAndAddFileRecord(
      FileItemInput file, String srcSystem, String srcEnv, String targetConnectionName)
      throws IOException {
    Path filePath = Path.of(fileDir, UUID.randomUUID().toString());
    long copied = Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

    FileEntity fileEntity =
        FileEntity.builder()
            .sourceSystem(srcSystem)
            .sourceEnvironment(srcEnv)
            .targetConnectionName(targetConnectionName)
            .savedFilename(filePath.getFileName().toString())
            .contentType(file.getContentType())
            .originalFilename(file.getName())
            .md5Checksum(getMd5FromFile(filePath))
            .sizeBytes(copied)
            .build();

    if (file.getName().contains("error")) {
      throw new IOException("Error in file");
    }
    return fileRepository.save(fileEntity);
  }

  private String getMd5FromFile(Path filePath) throws IOException {
    try (InputStream inputStream = Files.newInputStream(filePath)) {
      return DigestUtils.md5DigestAsHex(inputStream);
    }
  }
}
