package ch.dulce.largefileupload.controller;

import ch.dulce.largefileupload.exception.BadRequestException;
import ch.dulce.largefileupload.exception.SuccessResponse;
import ch.dulce.largefileupload.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/")
@Slf4j
@RequiredArgsConstructor
public class FileController {

  private static final String SOURCE_SYSTEM = "sourceSystem";
  private static final String SOURCE_ENVIRONMENT = "sourceEnvironment";
  private static final String TARGET_CONNECTION_NAME = "targetConnectionName";
  private final FileService fileService;

  @Transactional
  @PostMapping(path = "upload")
  public ResponseEntity<Object> upload(
      @RequestHeader(value = SOURCE_SYSTEM, required = false) String srcSystem,
      @RequestHeader(value = SOURCE_ENVIRONMENT, required = false) String srcEnv,
      @RequestHeader(value = TARGET_CONNECTION_NAME, required = false) String targetConnectionName,
      HttpServletRequest request)
      throws IOException {

    if (!StringUtils.hasText(srcSystem)) {
      throw new BadRequestException("Http header sourceSystem is required!");
    }
    if (!StringUtils.hasText(srcEnv)) {
      throw new BadRequestException("Http header sourceEnvironment is required!");
    }
    if (!StringUtils.hasText(targetConnectionName)) {
      throw new BadRequestException("Http header targetConnectionName is required!");
    }
    if (!JakartaServletFileUpload.isMultipartContent(request)) {
      throw new BadRequestException("Multipart request expected");
    }
    log.info("Received upload request from system {}, environment {}", srcSystem, srcEnv);

    JakartaServletFileUpload fileUpload = new JakartaServletFileUpload();
    List<UUID> tracingIds = new ArrayList<>();

    AtomicBoolean fileFound = new AtomicBoolean(false);
    fileUpload
        .getItemIterator(request)
        .forEachRemaining(
            item -> {
              if (!item.isFormField()) {
                if (!StringUtils.hasText(item.getName())) {
                  throw new BadRequestException("Original file name is required!");
                }
                fileFound.set(true);

                FileDto fileDto =
                    FileDto.builder()
                        .fileItem(item)
                        .srcSystem(srcSystem)
                        .srcEnv(srcEnv)
                        .targetConnectionName(targetConnectionName)
                        .build();
                FileResponse response = fileService.saveAndAddFileRecord(fileDto);
                if (response.getSizeBytes() == 0) {
                  throw new BadRequestException("Empty file received!");
                }

                log.info(
                    "Successfully saved file {} to local storage with id: {}.",
                    response.getOriginalFilename(),
                    response.getFileTracingId());
                tracingIds.add(response.getFileTracingId());
              }
            });

    if (!fileFound.get()) {
      throw new BadRequestException("No file submitted!");
    }
    return ResponseEntity.ok(
        new SuccessResponse(LocalDateTime.now(), "Successfully received file(s).", tracingIds));
  }
}
