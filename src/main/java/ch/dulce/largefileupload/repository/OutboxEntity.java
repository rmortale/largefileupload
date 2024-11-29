package ch.dulce.largefileupload.repository;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.*;

@Entity
@Table(name = "outbox")
@Getter
@Setter
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class OutboxEntity {

  @Id private UUID id;
  private String aggregatetype;
  private String aggregateid;
  private String type;

  @Column(length = 4096)
  private String payload;
}
