package com.examplatform.modules.evaluation.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.entity.EvaluationDataset;
import com.examplatform.modules.evaluation.entity.EvaluationQuestion;
import com.examplatform.modules.evaluation.mapper.EvaluationQuestionMapper;
import com.examplatform.modules.evaluation.repository.EvaluationDatasetRepository;
import com.examplatform.modules.evaluation.repository.EvaluationQuestionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationQuestionService {

    private final EvaluationQuestionRepository questionRepository;
    private final EvaluationDatasetRepository datasetRepository;
    private final EvaluationQuestionMapper questionMapper;

    @Transactional
    public QuestionResponse create(QuestionCreateRequest req) {
        EvaluationDataset dataset = datasetRepository.findById(req.getDatasetId())
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationDataset", req.getDatasetId()));
        EvaluationQuestion question = questionMapper.toEntity(req, dataset);
        return questionMapper.toResponse(questionRepository.save(question));
    }

    @Transactional
    public List<QuestionResponse> bulkCreate(QuestionBulkUploadRequest req) {
        EvaluationDataset dataset = datasetRepository.findById(req.getDatasetId())
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationDataset", req.getDatasetId()));

        List<EvaluationQuestion> questions = req.getQuestions().stream()
                .map(item -> questionMapper.toEntity(item, dataset))
                .toList();

        return questionRepository.saveAll(questions).stream()
                .map(questionMapper::toResponse)
                .toList();
    }

    @Transactional
    public QuestionResponse update(String id, QuestionUpdateRequest req) {
        EvaluationQuestion question = getOrThrow(id);
        questionMapper.applyUpdate(question, req);
        return questionMapper.toResponse(questionRepository.save(question));
    }

    public QuestionResponse getById(String id) {
        return questionMapper.toResponse(getOrThrow(id));
    }

    public List<QuestionResponse> listByDataset(String datasetId) {
        return questionRepository.findByDatasetId(datasetId).stream()
                .map(questionMapper::toResponse)
                .toList();
    }

    @Transactional
    public void delete(String id) {
        questionRepository.delete(getOrThrow(id));
    }

    private EvaluationQuestion getOrThrow(String id) {
        return questionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationQuestion", id));
    }
}
