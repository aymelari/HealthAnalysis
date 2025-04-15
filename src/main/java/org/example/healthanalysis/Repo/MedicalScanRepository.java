package org.example.healthanalysis.Repo;

import org.example.healthanalysis.Entity.MedicalScanEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MedicalScanRepository extends JpaRepository<MedicalScanEntity,Long> {
}
