package ch.dulce.largefileupload.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class FileEntity {

  @Id private UUID savedFilename;
  private String sourceSystem;
  private String sourceEnvironment;
  private String originalFilename;
  private String contentType;
  private String md5Checksum;
  private long size;
  private LocalDateTime uploadedAt;
}
