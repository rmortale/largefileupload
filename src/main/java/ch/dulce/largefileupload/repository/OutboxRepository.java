package ch.dulce.largefileupload.repository;

import ch.dulce.largefileupload.entity.OutboxEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEntity, UUID> {}
