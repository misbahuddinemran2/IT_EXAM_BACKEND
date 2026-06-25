package com.examplatform.modules.question.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.question.dto.request.AttemptRequest;
import com.examplatform.modules.question.dto.response.QuestionAnalyticsResponse;
import com.examplatform.modules.question.entity.Option;
import com.examplatform.modules.question.entity.Question;
import com.examplatform.modules.question.entity.QuestionAnalytics;
import com.examplatform.modules.question.repository.OptionRepository;
import com.examplatform.modules.question.repository.QuestionAnalyticsRepository;
import com.examplatform.modules.question.repository.QuestionRepository;
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
public class QuestionAnalyticsService {

    private final QuestionAnalyticsRepository analyticsRepository;
    private final QuestionRepository questionRepository;
    private final OptionRepository optionRepository;

    @Transactional
    public void recordAttempt(AttemptRequest request) {

        Question question = questionRepository
                .findById(request.getQuestionId())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Question", request.getQuestionId()));

        // Get or create analytics
        QuestionAnalytics analytics = analyticsRepository
                .findByQuestionId(request.getQuestionId())
                .orElseGet(() -> {
                    QuestionAnalytics newA = QuestionAnalytics
                            .builder()
                            .id(UUID.randomUUID().toString())
                            .question(question)
                            .build();
                    return analyticsRepository.save(newA);
                });

        if (request.isSkipped()) {
            analytics.setSkipCount(analytics.getSkipCount() + 1);
        } else {
            // Check if correct
            boolean isCorrect = false;
            if (request.getSelectedOptionId() != null) {
                Option selected = optionRepository
                        .findById(request.getSelectedOptionId())
                        .orElse(null);
                if (selected != null) {
                    isCorrect = selected.isCorrect();

                    // Update option selection count
                    selected.setSelectionCount(
                        selected.getSelectionCount() + 1);
                    optionRepository.save(selected);
                }
            }

            analytics.setTotalAttempts(
                analytics.getTotalAttempts() + 1);

            if (isCorrect) {
                analytics.setCorrectAttempts(
                    analytics.getCorrectAttempts() + 1);
            }

            // Update average time
            if (request.getTimeSpentSec() > 0) {
                double currentAvg = analytics.getAvgTimeSpentSec();
                long total = analytics.getTotalAttempts();
                double newAvg = ((currentAvg * (total - 1))
                        + request.getTimeSpentSec()) / total;
                analytics.setAvgTimeSpentSec(newAvg);
            }
        }

        analytics.setLastComputedAt(LocalDateTime.now());
        analyticsRepository.save(analytics);

        log.info("Attempt recorded for question: {}",
                request.getQuestionId());
    }

    public QuestionAnalyticsResponse getAnalytics(
            String questionId) {

        QuestionAnalytics analytics = analyticsRepository
                .findByQuestionId(questionId)
                .orElse(null);

        Question question = questionRepository
                .findById(questionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Question", questionId));

        if (analytics == null) {
            return QuestionAnalyticsResponse.builder()
                    .questionId(questionId)
                    .questionText(question.getQuestionText())
                    .totalAttempts(0)
                    .correctAttempts(0)
                    .skipCount(0)
                    .accuracyRate(0)
                    .avgTimeSpentSec(0)
                    .build();
        }

        return toResponse(analytics);
    }

    public List<QuestionAnalyticsResponse> getHardQuestions() {
        return analyticsRepository
                .findHardQuestions(50.0)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public List<QuestionAnalyticsResponse> getMostAttempted() {
        return analyticsRepository
                .findMostAttempted()
                .stream()
                .limit(10)
                .map(this::toResponse)
                .toList();
    }

    private QuestionAnalyticsResponse toResponse(
            QuestionAnalytics a) {
        return QuestionAnalyticsResponse.builder()
                .questionId(a.getQuestion().getId())
                .questionText(a.getQuestion().getQuestionText())
                .totalAttempts(a.getTotalAttempts())
                .correctAttempts(a.getCorrectAttempts())
                .skipCount(a.getSkipCount())
                .accuracyRate(a.getAccuracyRate())
                .avgTimeSpentSec(a.getAvgTimeSpentSec())
                .difficultyScoreActual(a.getDifficultyScoreActual())
                .lastComputedAt(a.getLastComputedAt() != null ?
                    a.getLastComputedAt().toString() : null)
                .build();
    }
}