package org.example.healthanalysis.Entity;

import jakarta.persistence.*;

import java.util.Date;

@Entity
@Table(name="scan")
public class MedicalScanEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;
//    private String scanType; // X-ray, MRI, ECG
    private String filePath; // Path to file storage (AWS S3, Local)
    private Date uploadDate;
    @ManyToOne
    private UserEntity user;
}
