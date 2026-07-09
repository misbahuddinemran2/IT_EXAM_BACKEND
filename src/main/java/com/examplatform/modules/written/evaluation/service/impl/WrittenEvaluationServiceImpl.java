package com.examplatform.modules.written.evaluation.service.impl;

import com.examplatform.modules.written.evaluation.entity.WrittenEvaluation;
import com.examplatform.modules.written.evaluation.mapper.WrittenEvaluationMapper;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationDetailRepository;
import com.examplatform.modules.written.evaluation.repository.WrittenEvaluationRepository;
import com.examplatform.modules.written.evaluation.response.EvaluationResponse;
import com.examplatform.modules.written.evaluation.service.WrittenEvaluationService;
import com.examplatform.modules.written.submission.repository.WrittenSubmissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
public class WrittenEvaluationServiceImpl implements WrittenEvaluationService {

    private final WrittenEvaluationRepository evaluationRepository;
    private final WrittenEvaluationDetailRepository detailRepository;
    private final WrittenEvaluationMapper evaluationMapper;
    private final WrittenSubmissionRepository submissionRepository;

    @Override
    public EvaluationResponse getEvaluationBySubmissionId(String submissionId) {
        WrittenEvaluation evaluation = evaluationRepository.findBySubmissionId(submissionId)
                .orElseThrow(() -> new NoSuchElementException("Evaluation not found for submission: " + submissionId));
        return evaluationMapper.toResponse(evaluation, detailRepository.findByEvaluationId(evaluation.getId()));
    }

    @Override
    public EvaluationResponse getEvaluationById(String evaluationId) {
        WrittenEvaluation evaluation = getEvaluationOrThrow(evaluationId);
        return evaluationMapper.toResponse(evaluation, detailRepository.findByEvaluationId(evaluation.getId()));
    }

    @Override
    public List<EvaluationResponse> getEvaluationsForExam(String examId) {
        List<String> submissionIds = submissionRepository.findByExamId(examId).stream()
                .map(s -> s.getId())
                .toList();

        return evaluationRepository.findAll().stream()
                .filter(e -> submissionIds.contains(e.getSubmission().getId()))
                .map(e -> evaluationMapper.toResponse(e, detailRepository.findByEvaluationId(e.getId())))
                .toList();
    }

    private WrittenEvaluation getEvaluationOrThrow(String evaluationId) {
        return evaluationRepository.findById(evaluationId)
                .orElseThrow(() -> new NoSuchElementException("Evaluation not found: " + evaluationId));
    }
}