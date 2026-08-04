package com.examplatform.modules.evaluation.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.evaluation.dto.RunResponse;
import com.examplatform.modules.evaluation.dto.RunSummaryResponse;
import com.examplatform.modules.evaluation.dto.RunTriggerRequest;
import com.examplatform.modules.evaluation.entity.*;
import com.examplatform.modules.evaluation.enums.EvaluationRunStatus;
import com.examplatform.modules.evaluation.mapper.EvaluationRunMapper;
import com.examplatform.modules.evaluation.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class EvaluationRunService {

    private final EvaluationRunRepository runRepository;
    private final EvaluationDatasetRepository datasetRepository;
    private final EvaluationProfileRepository profileRepository;
    private final EvaluationPromptRepository promptRepository;
    private final EvaluationQuestionRepository questionRepository;
    private final AdminUserRepository adminUserRepository;
    private final EvaluationRunMapper runMapper;

    /**
     * নতুন run তৈরি করে PENDING স্ট্যাটাসে — এখনো execute হয় না।
     * Execution আলাদাভাবে EvaluationRunnerService.executeRun() দিয়ে হয়
     * (Controller phase-এ দুই ধাপ আলাদা করা হবে যাতে ভবিষ্যতে queue/async করা সহজ হয়)।
     */
    @Transactional
    public RunResponse createRun(RunTriggerRequest req, String adminId) {
        EvaluationDataset dataset = datasetRepository.findById(req.getDatasetId())
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationDataset", req.getDatasetId()));
        EvaluationProfile profile = profileRepository.findById(req.getProfileId())
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationProfile", req.getProfileId()));
        EvaluationPrompt prompt = promptRepository.findById(req.getPromptId())
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationPrompt", req.getPromptId()));

        long questionCount = questionRepository.countByDatasetIdAndIsActiveTrue(dataset.getId());
        if (questionCount == 0) {
            throw new IllegalStateException("এই dataset-এ কোনো active প্রশ্ন নেই");
        }

        EvaluationRun run = EvaluationRun.builder()
                .dataset(dataset)
                .datasetVersion(req.getDatasetVersion())
                .profile(profile)
                .prompt(prompt)
                .configurationSnapshot(runMapper.buildConfigurationSnapshot(profile, prompt))
                .status(EvaluationRunStatus.PENDING)
                .totalQuestions((int) questionCount)
                .processedQuestions(0)
                .triggeredByAdmin(adminId != null ? adminUserRepository.findById(adminId)
                        .orElseThrow(() -> new NoSuchElementException("Admin not found: " + adminId)) : null)
                .build();

        return runMapper.toResponse(runRepository.save(run));
    }

    public RunResponse getById(String id) {
        return runMapper.toResponse(getOrThrow(id));
    }

    public List<RunSummaryResponse> listByDataset(String datasetId) {
        return runRepository.findByDatasetIdOrderByCreatedAtDesc(datasetId).stream()
                .map(runMapper::toSummaryResponse)
                .toList();
    }

    public List<RunSummaryResponse> listByIds(List<String> ids) {
        return runRepository.findByIdInOrderByCreatedAtAsc(ids).stream()
                .map(runMapper::toSummaryResponse)
                .toList();
    }

    EvaluationRun getOrThrow(String id) {
        return runRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationRun", id));
    }
}
