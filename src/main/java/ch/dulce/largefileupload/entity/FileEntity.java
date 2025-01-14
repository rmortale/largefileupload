package ch.dulce.largefileupload.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FileEntity {

  @Id @GeneratedValue private UUID id;
  private String savedFilename;
  private String sourceSystem;
  private String sourceEnvironment;
  private String originalFilename;
  private String contentType;
  private String md5Checksum;
  private String targetConnectionName;
  private long sizeBytes;
  @CreatedDate private LocalDateTime receivedAt;
}
