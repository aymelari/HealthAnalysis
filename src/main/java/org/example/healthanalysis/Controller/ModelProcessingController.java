package org.example.healthanalysis.Controller;

import ai.onnxruntime.OrtException;
import lombok.RequiredArgsConstructor;

import org.example.healthanalysis.Service.AlzheimerService;
import org.example.healthanalysis.Service.LungService;
import org.example.healthanalysis.Service.MedicalService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
public class ModelProcessingController {
    public final AlzheimerService alzheimerService;
    private final LungService lungService;
    private final MedicalService medicalService;

    @PostMapping(value = "/alz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> predictAlz(@RequestParam("file") MultipartFile file) throws IOException {
        try {
            String prediction = alzheimerService.predictFromFile(file);
            return ResponseEntity.ok(prediction);
        } catch (IOException | OrtException e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body("Prediction failed: " + e.getMessage());
        }
    }

    @PostMapping(value="/lung" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Map<String, Object> predictLung(@RequestParam("image") MultipartFile file) throws Exception {
        return lungService.predict(file);
    }
}
