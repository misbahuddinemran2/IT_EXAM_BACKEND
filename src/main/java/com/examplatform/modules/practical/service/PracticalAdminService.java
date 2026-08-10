package com.examplatform.modules.practical.service;

import com.examplatform.modules.practical.dto.ExperimentAdminRequest;
import com.examplatform.modules.practical.entity.*;
import com.examplatform.modules.practical.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PracticalAdminService {

    private final PracticalChapterRepository chapterRepository;
    private final PracticalExperimentRepository experimentRepository;
    private final PracticalKhataRepository khataRepository;
    private final PracticalVivaQuestionRepository vivaQuestionRepository;

    // ============================================
    // চ্যাপ্টার লিস্ট (admin dropdown-এর জন্য)
    // ============================================
    public List<PracticalChapter> getAllChapters() {
        return chapterRepository.findAllByOrderByOrderNumberAsc();
    }

    // ============================================
    // একটা চ্যাপ্টারের সব experiment (active + inactive, admin দেখবে)
    // ============================================
    public List<PracticalExperiment> getExperimentsByChapter(String chapterId) {
        return experimentRepository.findByChapterIdOrderByOrderNumberAsc(chapterId);
    }

    // ============================================
    // নতুন experiment তৈরি (খাতা + viva সহ, একবারেই)
    // ============================================
    @Transactional
    public PracticalExperiment createExperiment(ExperimentAdminRequest req, String adminId) {
        LocalDateTime now = LocalDateTime.now();
        String experimentId = UUID.randomUUID().toString();

        PracticalExperiment experiment = PracticalExperiment.builder()
                .id(experimentId)
                .chapterId(req.getChapterId())
                .title(req.getTitle())
                .titleBn(req.getTitleBn())
                .description(req.getDescription())
                .isActive(req.isActive())
                .targetSessions(req.getTargetSessions() == null || req.getTargetSessions().isEmpty()
                        ? List.of("ALL") : req.getTargetSessions())
                .orderNumber(req.getOrderNumber())
                .createdBy(adminId)
                .createdAt(now)
                .updatedAt(now)
                .build();
        experimentRepository.save(experiment);

        saveKhata(experimentId, req);
        saveVivaQuestions(experimentId, req);

        log.info("Practical experiment created: {} ({})", experiment.getTitle(), experimentId);
        return experiment;
    }

    // ============================================
    // Experiment আপডেট (খাতা + viva রিপ্লেস)
    // ============================================
    @Transactional
    public PracticalExperiment updateExperiment(String experimentId, ExperimentAdminRequest req) {
        PracticalExperiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new RuntimeException("Experiment not found"));

        experiment.setTitle(req.getTitle());
        experiment.setTitleBn(req.getTitleBn());
        experiment.setDescription(req.getDescription());
        experiment.setActive(req.isActive());
        experiment.setTargetSessions(req.getTargetSessions() == null || req.getTargetSessions().isEmpty()
                ? List.of("ALL") : req.getTargetSessions());
        experiment.setOrderNumber(req.getOrderNumber());
        experiment.setUpdatedAt(LocalDateTime.now());
        experimentRepository.save(experiment);

        saveKhata(experimentId, req);

        vivaQuestionRepository.deleteByExperimentId(experimentId);
        saveVivaQuestions(experimentId, req);

        log.info("Practical experiment updated: {}", experimentId);
        return experiment;
    }

    // ============================================
    // শুধু active/inactive toggle (quick action)
    // ============================================
    @Transactional
    public void toggleActive(String experimentId, boolean isActive) {
        PracticalExperiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new RuntimeException("Experiment not found"));
        experiment.setActive(isActive);
        experiment.setUpdatedAt(LocalDateTime.now());
        experimentRepository.save(experiment);
    }

    // ============================================
    // Experiment ডিলিট
    // ============================================
    @Transactional
    public void deleteExperiment(String experimentId) {
        vivaQuestionRepository.deleteByExperimentId(experimentId);
        khataRepository.findByExperimentId(experimentId).ifPresent(khataRepository::delete);
        experimentRepository.deleteById(experimentId);
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================
    private void saveKhata(String experimentId, ExperimentAdminRequest req) {
        if (req.getKhataType() == null) return;

        PracticalKhata khata = khataRepository.findByExperimentId(experimentId)
                .orElse(PracticalKhata.builder()
                        .id(UUID.randomUUID().toString())
                        .experimentId(experimentId)
                        .createdAt(LocalDateTime.now())
                        .build());

        khata.setKhataType(PracticalKhata.KhataType.valueOf(req.getKhataType().toUpperCase()));
        khata.setPdfUrl(req.getPdfUrl());
        khata.setTextContent(req.getTextContent());
        khata.setUpdatedAt(LocalDateTime.now());

        khataRepository.save(khata);
    }

    private void saveVivaQuestions(String experimentId, ExperimentAdminRequest req) {
        if (req.getVivaQuestions() == null) return;

        for (ExperimentAdminRequest.VivaItem item : req.getVivaQuestions()) {
            PracticalVivaQuestion viva = PracticalVivaQuestion.builder()
                    .id(UUID.randomUUID().toString())
                    .experimentId(experimentId)
                    .question(item.getQuestion())
                    .questionBn(item.getQuestionBn())
                    .answer(item.getAnswer())
                    .orderNumber(item.getOrderNumber())
                    .createdAt(LocalDateTime.now())
                    .build();
            vivaQuestionRepository.save(viva);
        }
    }
}
