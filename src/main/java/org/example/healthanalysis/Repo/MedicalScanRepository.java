package org.example.healthanalysis.Repo;

import org.example.healthanalysis.Entity.MedicalScanEntity;
import org.example.healthanalysis.Entity.UserEntity;
import org.example.healthanalysis.Service.MedicalScanService;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Set;

public interface MedicalScanRepository extends JpaRepository<MedicalScanEntity,Long> {
    Set<MedicalScanEntity> findByUser(UserEntity user);
    Set<MedicalScanEntity> findByModelType(String modelType);
}
