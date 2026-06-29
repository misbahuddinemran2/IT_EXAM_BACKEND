package com.examplatform.appconfig.service;

import com.examplatform.appconfig.dto.OnboardingSlideRequest;
import com.examplatform.appconfig.dto.SlideReorderRequest;
import com.examplatform.appconfig.entity.OnboardingSlide;
import com.examplatform.appconfig.repository.OnboardingSlideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@RequiredArgsConstructor
public class OnboardingService {

    private final OnboardingSlideRepository repo;

    public List<OnboardingSlide> getAll() {
        return repo.findAllByOrderBySlideOrder();
    }

    public List<OnboardingSlide> getActive() {
        return repo.findByIsActiveTrueOrderBySlideOrder();
    }

    @Transactional
    public OnboardingSlide create(OnboardingSlideRequest req) {
        OnboardingSlide slide = OnboardingSlide.builder()
                .title(req.getTitle())
                .subtitle(req.getSubtitle())
                .description(req.getDescription())
                .imageUrl(req.getImageUrl())
                .animationUrl(req.getAnimationUrl())
                .slideOrder(req.getSlideOrder() != null ? req.getSlideOrder() : 99)
                .isActive(req.getIsActive() != null ? req.getIsActive() : true)
                .build();
        return repo.save(slide);
    }

    @Transactional
    public OnboardingSlide update(String id, OnboardingSlideRequest req) {
        OnboardingSlide slide = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Slide not found: " + id));
        slide.setTitle(req.getTitle());
        slide.setSubtitle(req.getSubtitle());
        slide.setDescription(req.getDescription());
        if (req.getImageUrl() != null) slide.setImageUrl(req.getImageUrl());
        if (req.getAnimationUrl() != null) slide.setAnimationUrl(req.getAnimationUrl());
        if (req.getSlideOrder() != null) slide.setSlideOrder(req.getSlideOrder());
        if (req.getIsActive() != null) slide.setIsActive(req.getIsActive());
        return repo.save(slide);
    }

    @Transactional
    public void delete(String id) {
        repo.deleteById(id);
    }

    @Transactional
    public List<OnboardingSlide> reorder(SlideReorderRequest req) {
        AtomicInteger order = new AtomicInteger(1);
        req.getSlideIds().forEach(id -> repo.findById(id).ifPresent(slide -> {
            slide.setSlideOrder(order.getAndIncrement());
            repo.save(slide);
        }));
        return repo.findByIsActiveTrueOrderBySlideOrder();
    }

    @Transactional
    public OnboardingSlide toggleStatus(String id) {
        OnboardingSlide slide = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Slide not found: " + id));
        slide.setIsActive(!slide.getIsActive());
        return repo.save(slide);
    }
}