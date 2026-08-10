package com.examplatform.modules.practical.repository;

import com.examplatform.modules.practical.entity.PracticalKhata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PracticalKhataRepository extends JpaRepository<PracticalKhata, String> {
    Optional<PracticalKhata> findByExperimentId(String experimentId);
}
