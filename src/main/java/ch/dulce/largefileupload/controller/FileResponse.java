package ch.dulce.largefileupload.controller;

import java.util.UUID;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class FileResponse {
  private String originalFilename;
  private long sizeBytes;
  private UUID fileTracingId;
}
