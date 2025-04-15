package org.example.healthanalysis.Repo;

import org.example.healthanalysis.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
