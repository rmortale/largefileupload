package ch.dulce.largefileupload.repository;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
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
@Builder
public class FileEntity {

  @Id @GeneratedValue private UUID id;
  private String savedFilename;
  private String sourceSystem;
  private String sourceEnvironment;
  private String originalFilename;
  private String contentType;
  private String md5Checksum;
  private String targetConnectionName;
  private String status;
  private String errorMsg;
  private long sizeBytes;
  private LocalDateTime receivedAt;
  private LocalDateTime deliveredAt;
  private LocalDateTime nextRetryTime;
  private int deliveryRetriedNum;
}
