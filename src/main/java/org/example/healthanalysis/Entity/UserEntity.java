package org.example.healthanalysis.Entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class UserEntity {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private long id;
    private String firstName;
    private String lastName;
    private String email;

}
