package org.example.healthanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime;

@Getter
@Setter
@Builder
public class MedicalScanResponseDto {
    Long id;
    private String path;
    @Schema(type = "string", format = "date-time", example = "2024-05-15T14:30:00+05:30")// Path to file storage (AWS S3, Local)
    private OffsetDateTime uploadDate;
    private Long userId;
    private String modelType;
}
