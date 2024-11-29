package ch.dulce.largefileupload.event;

import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class FileUploadedEvent {
  private UUID id;
  private String originalFilename;
  private long size;
  private String contentType;
  private String savedFilename;
  private String sourceEnvironment;
  private String sourceSystem;
  private LocalDateTime uploadDate;
  private String checksum;
}
