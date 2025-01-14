package ch.dulce.largefileupload.controller;

import ch.dulce.largefileupload.exception.BadRequestException;
import ch.dulce.largefileupload.service.FileService;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload2.jakarta.JakartaServletFileUpload;
import org.springframework.http.ResponseEntity;
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

  @PostMapping(path = "upload")
  public ResponseEntity<Object> upload(
      @RequestHeader(value = SOURCE_SYSTEM, required = false) String srcSystem,
      @RequestHeader(value = SOURCE_ENVIRONMENT, required = false) String srcEnv,
      @RequestHeader(value = TARGET_CONNECTION_NAME, required = false) String targetConnectionName,
      HttpServletRequest request)
      throws IOException {

    validateRequest(request, srcSystem, srcEnv, targetConnectionName);

    log.info("Received upload request from system {}, environment {}", srcSystem, srcEnv);

    List<UUID> uuids =
        fileService.uploadFiles(
            new JakartaServletFileUpload().getItemIterator(request),
            srcSystem,
            srcEnv,
            targetConnectionName);

    return ResponseEntity.ok(
        new SuccessResponse(LocalDateTime.now(), "Successfully received file(s).", uuids));
  }

  private void validateRequest(
      HttpServletRequest request, String srcSystem, String srcEnv, String targetConnectionName) {
    if (!JakartaServletFileUpload.isMultipartContent(request)) {
      throw new BadRequestException("Multipart request expected");
    }
    if (!StringUtils.hasText(srcSystem)) {
      throw new BadRequestException("Http header sourceSystem is required!");
    }
    if (!StringUtils.hasText(srcEnv)) {
      throw new BadRequestException("Http header sourceEnvironment is required!");
    }
    if (!StringUtils.hasText(targetConnectionName)) {
      throw new BadRequestException("Http header targetConnectionName is required!");
    }
  }
}
