package org.example.healthanalysis.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.Set;

@Entity
@Table

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long id;
    private String firstName;
    private String lastName;
    private String email;
    @OneToMany(mappedBy = "user")
    private Set<MedicalScanEntity> medicalScan;
}
