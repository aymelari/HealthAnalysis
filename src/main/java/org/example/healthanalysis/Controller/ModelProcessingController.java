package org.example.healthanalysis.Controller;

import ai.onnxruntime.OrtException;
import lombok.RequiredArgsConstructor;

import org.example.healthanalysis.Service.AlzheimerService;
import org.example.healthanalysis.Service.LungService;
import org.example.healthanalysis.Service.MedicalScanService;
import org.example.healthanalysis.dto.MedicalScanRequestDto;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/model")
@RequiredArgsConstructor
public class ModelProcessingController {
    public final AlzheimerService alzheimerService;
    private final LungService lungService;
    private final MedicalScanService medicalService;

    @PostMapping(value = "/alz", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> predictAlz(@ModelAttribute  MedicalScanRequestDto medicalScanRequestDto) throws IOException {

            String path=medicalService.saveFileToDisk(medicalScanRequestDto.getMultipartFile());
            String prediction = alzheimerService.callPredictAPI(medicalScanRequestDto,path);
            return ResponseEntity.ok(prediction);
    }

    @PostMapping(value="/lung" ,consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> predictLung(@ModelAttribute  MedicalScanRequestDto medicalScanRequestDto) throws Exception {
        String path=medicalService.saveFileToDisk(medicalScanRequestDto.getMultipartFile());
        String predict = lungService.callPredictAPI(medicalScanRequestDto,path);

        return ResponseEntity.ok(predict);
    }



}
