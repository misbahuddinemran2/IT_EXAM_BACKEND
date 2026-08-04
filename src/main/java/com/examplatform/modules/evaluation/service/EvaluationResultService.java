package com.examplatform.modules.evaluation.service;

import com.examplatform.common.exception.ResourceNotFoundException;
import com.examplatform.modules.evaluation.dto.ResultResponse;
import com.examplatform.modules.evaluation.dto.ResultSummaryResponse;
import com.examplatform.modules.evaluation.entity.EvaluationResult;
import com.examplatform.modules.evaluation.mapper.EvaluationResultMapper;
import com.examplatform.modules.evaluation.repository.EvaluationResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.examplatform.common.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class EvaluationResultService {

    private final EvaluationResultRepository resultRepository;
    private final EvaluationResultMapper resultMapper;

    public ResultResponse getById(String id) {
        EvaluationResult result = resultRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("EvaluationResult", id));
        return resultMapper.toResponse(result);
    }

    public Page<ResultSummaryResponse> listByRun(String runId, Pageable pageable) {
        return resultRepository.findByRunId(runId, pageable).map(resultMapper::toSummaryResponse);
    }
}
