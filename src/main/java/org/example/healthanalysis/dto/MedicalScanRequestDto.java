package org.example.healthanalysis.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Access;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.OffsetDateTime; // or Instant/ZonedDateTime
import java.util.Date;

import io.swagger.v3.oas.annotations.media.Schema;

@Getter
@Setter

public class MedicalScanRequestDto {
    @Schema(type = "string", format = "date-time", example = "2024-05-15T14:30:00+05:30")
    private OffsetDateTime uploadDate;
    private Long userId;
    @Schema(type = "string", format = "binary", description = "Medical scan file")
    private MultipartFile multipartFile;

}
