package org.example.healthanalysis.Controller;

import org.springframework.core.io.Resource;
import lombok.RequiredArgsConstructor;
import org.example.healthanalysis.Entity.MedicalScanEntity;
import org.example.healthanalysis.Repo.MedicalScanRepository;
import org.example.healthanalysis.Service.MedicalScanService;
import org.example.healthanalysis.dto.MedicalScanResponseDto;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/scan")
public class AppController {
    private final MedicalScanService medicalScanService;
    private final MedicalScanRepository scanRepository;
    private final MedicalScanRepository medicalScanRepository;

    @GetMapping("all/{userId}")
    public ResponseEntity<Set<Long>> getAllMedicanScans(@RequestBody @PathVariable Long userId){
        Set<Long> allMedicalScan = medicalScanService.getAllMedicalScan(userId);
        return ResponseEntity.ok(allMedicalScan);
    }

    @GetMapping("{id}")
    public ResponseEntity<MedicalScanResponseDto> getMedicalScanById(@PathVariable @RequestBody Long id) throws IOException {
        return ResponseEntity.ok(medicalScanService.getMedicalScanById(id));
    }

    @GetMapping("/scans/download/{id}")
    public ResponseEntity<Resource> downloadScan(@PathVariable Long id) throws FileNotFoundException {
        MedicalScanEntity scan = medicalScanRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("medical scan not found"));

        File file = new File(scan.getFilePath());
        Resource resource = (Resource) new InputStreamResource(new FileInputStream(file)); // Cast as Resource

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + file.getName())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(resource);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<Void> deleteMedicalScan(@RequestBody @PathVariable Long id) throws IOException {
       medicalScanService.deleteMedicalScanById(id);
       return ResponseEntity.noContent().build();
    }

    @GetMapping("/scan/{modelType}")
    public ResponseEntity<Set<MedicalScanResponseDto>> getMedicalScanByType(@RequestBody @PathVariable String modelType){
        return ResponseEntity.ok(medicalScanService.getMedicalScanByType(modelType));
    }


}
