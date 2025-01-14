package ch.dulce.largefileupload.repository;

import ch.dulce.largefileupload.entity.FileEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileRepository extends JpaRepository<FileEntity, UUID> {}
