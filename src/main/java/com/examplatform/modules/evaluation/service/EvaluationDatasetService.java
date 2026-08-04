package com.examplatform.modules.evaluation.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.evaluation.dto.*;
import com.examplatform.modules.evaluation.entity.EvaluationDataset;
import com.examplatform.modules.evaluation.mapper.EvaluationDatasetMapper;
import com.examplatform.modules.evaluation.repository.EvaluationDatasetRepository;
import com.examplatform.modules.evaluation.repository.EvaluationQuestionRepository;
import com.examplatform.modules.evaluation.repository.EvaluationRunRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationDatasetService {

    private final EvaluationDatasetRepository datasetRepository;
    private final EvaluationQuestionRepository questionRepository;
    private final EvaluationRunRepository runRepository;
    private final EvaluationDatasetMapper datasetMapper;

    @Transactional
    public DatasetResponse create(DatasetCreateRequest req, String adminId) {
        EvaluationDataset dataset = datasetMapper.toEntity(req, adminId);
        return datasetMapper.toResponse(datasetRepository.save(dataset));
    }

    @Transactional
    public DatasetResponse update(String id, DatasetUpdateRequest req) {
        EvaluationDataset dataset = getOrThrow(id);
        datasetMapper.applyUpdate(dataset, req);
        return datasetMapper.toResponse(datasetRepository.save(dataset));
    }

    public DatasetResponse getById(String id) {
        return datasetMapper.toResponse(getOrThrow(id));
    }

    public List<DatasetSummaryResponse> listAll() {
        return datasetRepository.findAll().stream()
                .map(d -> datasetMapper.toSummaryResponse(
                        d,
                        questionRepository.countByDatasetId(d.getId()),
                        runRepository.findByDatasetId(d.getId()).size()))
                .toList();
    }

    @Transactional
    public void delete(String id) {
        EvaluationDataset dataset = getOrThrow(id);
        // dataset-এর সাথে যুক্ত run থাকলে delete করা হবে না — historical experiment ডেটা সুরক্ষিত রাখতে
        if (!runRepository.findByDatasetId(id).isEmpty()) {
            throw new IllegalStateException("এই dataset-এর সাথে ইতিমধ্যে run যুক্ত আছে, delete করা যাবে না");
        }
        questionRepository.deleteByDatasetId(id);
        datasetRepository.delete(dataset);
    }

    private EvaluationDataset getOrThrow(String id) {
        return datasetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationDataset", id));
    }
}
