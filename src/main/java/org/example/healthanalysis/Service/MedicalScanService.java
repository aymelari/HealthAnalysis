package org.example.healthanalysis.Service;

import lombok.RequiredArgsConstructor;
import org.example.healthanalysis.Entity.MedicalScanEntity;
import org.example.healthanalysis.Entity.UserEntity;
import org.example.healthanalysis.Repo.MedicalScanRepository;
import org.example.healthanalysis.Repo.UserRepository;
import org.example.healthanalysis.dto.MedicalScanRequestDto;
import org.example.healthanalysis.dto.MedicalScanResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor

public class MedicalScanService {
    private final MedicalScanRepository medicalScanRepository;
    private final UserRepository userRepository;


    public String saveFileToDisk(MultipartFile file) throws IOException {
        String uploadDir = "C:\\Users\\aysua\\OneDrive\\Desktop\\Github Repos\\HealthAnalysis\\src\\main\\java\\org\\example\\healthanalysis\\uploads";
        String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        Path filePath = uploadPath.resolve(fileName);

        // ✅ Use copy instead of transferTo to avoid consuming the file
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return filePath.toString();
    }



    public class FileToMultipart implements MultipartFile {

        private final byte[] content;
        private final String fileName;

        public FileToMultipart(File file) throws IOException {
            this.fileName = file.getName();
            try (InputStream is = new FileInputStream(file)) {
                this.content = is.readAllBytes();
            }
        }

        @Override
        public String getName() {
            return fileName;
        }

        @Override
        public String getOriginalFilename() {
            return fileName;
        }

        @Override
        public String getContentType() {
            return "application/octet-stream";
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content;
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException {
            try (OutputStream os = new FileOutputStream(dest)) {
                os.write(content);
            }
        }
    }


    public void saveMedicalScan(MedicalScanRequestDto medicalScanRequestDto,String result,String modelType,String path){
        UserEntity user=userRepository.findById(medicalScanRequestDto.getUserId()).orElseThrow(()->new RuntimeException("User Not Found"));
        MedicalScanEntity medicalScanEntity=new MedicalScanEntity();
        medicalScanEntity.setUser(user);
        medicalScanEntity.setModelType(modelType);
        medicalScanEntity.setFilePath(path);
        medicalScanEntity.setUploadDate(medicalScanRequestDto.getUploadDate());

        medicalScanRepository.save(medicalScanEntity);


    }

    public Set<Long> getAllMedicalScan(Long userId){
        UserEntity user=userRepository.findById(userId).orElseThrow(()->new RuntimeException("User Not Found"));
        Set<MedicalScanEntity> medicalScanEntitySet=medicalScanRepository.findByUser(user);
        Set<Long> collect = medicalScanEntitySet.stream().map(MedicalScanEntity::getId).collect(Collectors.toSet());
        return collect;
    }

    public MedicalScanResponseDto getMedicalScanById(Long id) throws IOException {
        MedicalScanEntity medicalScanEntity=medicalScanRepository.findById(id).orElseThrow(()->new RuntimeException("MedicalScan Not Found"));
        System.out.println("user foud");
        File file = new File(medicalScanEntity.getFilePath());
        MultipartFile multipartFile = new FileToMultipart(file);
        System.out.println("file ready");

        MedicalScanResponseDto medicalScanResponseDto= MedicalScanResponseDto.builder()
                .id(id)
                .modelType(medicalScanEntity.getModelType())
                .uploadDate(medicalScanEntity.getUploadDate())
                .userId(medicalScanEntity.getUser().getId())
                .path(medicalScanEntity.getFilePath())
                .build();


        return medicalScanResponseDto;
    }

    public void deleteMedicalScanById(Long id){
        MedicalScanEntity scan = medicalScanRepository.findById(id).orElseThrow(() -> new RuntimeException("MedicalScan Not Found"));
        medicalScanRepository.deleteById(id);
    }


    public Set<MedicalScanResponseDto> getMedicalScanByType(String modelType){
        Set<MedicalScanEntity> byModelType = medicalScanRepository.findByModelType(modelType);
        if(byModelType.isEmpty()){
            return null;
        }
        Set<MedicalScanResponseDto> result=new HashSet<>();


        for(MedicalScanEntity medicalScanEntity:byModelType){
            MedicalScanResponseDto build = MedicalScanResponseDto.builder()
                    .id(medicalScanEntity.getId())
                    .modelType(medicalScanEntity.getModelType())
                    .uploadDate(medicalScanEntity.getUploadDate())
                    .userId(medicalScanEntity.getUser().getId())
                    .path(medicalScanEntity.getFilePath())
                    .build();
            result.add(build);

        }
        return result;

    }



}
