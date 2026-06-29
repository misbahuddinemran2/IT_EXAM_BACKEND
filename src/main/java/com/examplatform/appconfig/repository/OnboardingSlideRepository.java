package com.examplatform.appconfig.repository;

import com.examplatform.appconfig.entity.OnboardingSlide;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface OnboardingSlideRepository extends JpaRepository<OnboardingSlide, String> {

    List<OnboardingSlide> findByIsActiveTrueOrderBySlideOrder();

    List<OnboardingSlide> findAllByOrderBySlideOrder();
}