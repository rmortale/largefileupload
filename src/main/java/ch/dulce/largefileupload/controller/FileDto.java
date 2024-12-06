package ch.dulce.largefileupload.controller;

import lombok.Builder;
import lombok.Getter;
import org.apache.commons.fileupload2.core.FileItemInput;

@Builder
@Getter
public class FileDto {

  private final FileItemInput fileItem;
  private final String srcSystem;
  private final String srcEnv;
  private final String targetConnectionName;
}
