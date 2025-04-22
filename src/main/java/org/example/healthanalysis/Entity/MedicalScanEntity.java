package org.example.healthanalysis.Entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;
import java.util.Date;

@Entity
@Table(name="scan")
@Getter
@Setter
public class MedicalScanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String filePath; // Path to file storage (AWS S3, Local)
    private OffsetDateTime uploadDate;
    @ManyToOne
    private UserEntity user;
    private String modelType;
}
