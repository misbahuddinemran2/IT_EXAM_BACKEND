package com.examplatform.modules.practical.service;

import com.examplatform.modules.practical.dto.*;
import com.examplatform.modules.practical.entity.*;
import com.examplatform.modules.practical.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class PracticalStudentService {

    private final PracticalChapterRepository chapterRepository;
    private final PracticalExperimentRepository experimentRepository;
    private final PracticalKhataRepository khataRepository;
    private final PracticalVivaQuestionRepository vivaQuestionRepository;
    private final JdbcTemplate jdbcTemplate;

    // ============================================
    // 1. CHAPTER LIST (৩টা fixed chapter, প্রতিটার visible experiment count সহ)
    // ============================================
    public List<PracticalChapterResponse> getChapters(String userId) {
        String userSession = getUserSession(userId);

        List<PracticalChapter> chapters = chapterRepository.findAllByOrderByOrderNumberAsc();

        return chapters.stream()
                .map(ch -> {
                    List<PracticalExperiment> visible = filterVisible(
                            experimentRepository.findByChapterIdAndIsActiveTrueOrderByOrderNumberAsc(ch.getId()),
                            userSession
                    );
                    return PracticalChapterResponse.builder()
                            .id(ch.getId())
                            .name(ch.getName())
                            .nameBn(ch.getNameBn())
                            .icon(ch.getIcon())
                            .experimentCount(visible.size())
                            .build();
                })
                .collect(Collectors.toList());
    }

    // ============================================
    // 2. EXPERIMENT LIST (নির্দিষ্ট chapter-এর, session-filtered)
    // ============================================
    public List<PracticalExperimentResponse> getExperiments(String chapterId, String userId) {
        String userSession = getUserSession(userId);

        List<PracticalExperiment> experiments = filterVisible(
                experimentRepository.findByChapterIdAndIsActiveTrueOrderByOrderNumberAsc(chapterId),
                userSession
        );

        return experiments.stream()
                .map(e -> PracticalExperimentResponse.builder()
                        .id(e.getId())
                        .chapterId(e.getChapterId())
                        .title(e.getTitle())
                        .titleBn(e.getTitleBn())
                        .description(e.getDescription())
                        .build())
                .collect(Collectors.toList());
    }

    // ============================================
    // 3. EXPERIMENT DETAIL (খাতা + viva একসাথে)
    // ============================================
    public PracticalExperimentDetailResponse getExperimentDetail(String experimentId, String userId) {
        PracticalExperiment experiment = experimentRepository.findById(experimentId)
                .orElseThrow(() -> new RuntimeException("Experiment not found"));

        String userSession = getUserSession(userId);
        if (!isVisibleToSession(experiment, userSession)) {
            throw new RuntimeException("This experiment is not available for your session.");
        }
        if (!experiment.isActive()) {
            throw new RuntimeException("This experiment is currently inactive.");
        }

        PracticalKhata khata = khataRepository.findByExperimentId(experimentId).orElse(null);

        List<PracticalExperimentDetailResponse.VivaQuestionDto> vivaDtos =
                vivaQuestionRepository.findByExperimentIdOrderByOrderNumberAsc(experimentId).stream()
                        .map(v -> PracticalExperimentDetailResponse.VivaQuestionDto.builder()
                                .id(v.getId())
                                .question(v.getQuestion())
                                .questionBn(v.getQuestionBn())
                                .answer(v.getAnswer())
                                .build())
                        .collect(Collectors.toList());

        return PracticalExperimentDetailResponse.builder()
                .id(experiment.getId())
                .chapterId(experiment.getChapterId())
                .title(experiment.getTitle())
                .titleBn(experiment.getTitleBn())
                .description(experiment.getDescription())
                .khataType(khata != null ? khata.getKhataType().name() : null)
                .pdfUrl(khata != null ? khata.getPdfUrl() : null)
                .textContent(khata != null ? khata.getTextContent() : null)
                .vivaQuestions(vivaDtos)
                .build();
    }

    // ============================================
    // PRIVATE HELPERS
    // ============================================
    private List<PracticalExperiment> filterVisible(List<PracticalExperiment> experiments, String userSession) {
        return experiments.stream()
                .filter(e -> isVisibleToSession(e, userSession))
                .collect(Collectors.toList());
    }

    private boolean isVisibleToSession(PracticalExperiment experiment, String userSession) {
        List<String> targetSessions = experiment.getTargetSessions();
        if (targetSessions == null || targetSessions.isEmpty() || targetSessions.contains("ALL")) {
            return true;
        }
        if (userSession == null) return false;
        return targetSessions.contains(userSession);
    }

    private String getUserSession(String userId) {
        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(
                    "SELECT session FROM users WHERE id = ?", userId
            );
            if (!result.isEmpty()) {
                return (String) result.get(0).get("session");
            }
        } catch (Exception e) {
            log.warn("Could not fetch session for user: {}", userId);
        }
        return null;
    }
}
