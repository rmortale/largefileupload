package ch.dulce.largefileupload.entity;

import jakarta.persistence.*;
import java.util.UUID;
import lombok.*;

@Entity
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "file_upload_outbox")
public class OutboxEntity {

  @Id @GeneratedValue private UUID id;

  @Column(length = 4096)
  private String eventPayload;
}
