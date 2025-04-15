package org.example.healthanalysis.Service;

import lombok.RequiredArgsConstructor;
import org.example.healthanalysis.Entity.MedicalScanEntity;
import org.example.healthanalysis.Repo.MedicalScanRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class MedicalService {
    private final MedicalScanRepository medicalScanRepository;


    public String saveFileToDisk(MultipartFile file) throws IOException {
        String uploadDir = "C:\\Users\\aysua\\OneDrive\\Desktop\\Github Repos\\HealthAnalysis\\src\\main\\java\\org\\example\\healthanalysis\\uploads";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);
        file.transferTo(filePath.toFile());

        return filePath.toString(); // You can save this in your database
    }

}
