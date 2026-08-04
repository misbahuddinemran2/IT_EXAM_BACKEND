package com.examplatform.modules.evaluation.report;

import com.examplatform.modules.evaluation.dto.ResultResponse;
import com.examplatform.modules.evaluation.mapper.EvaluationResultMapper;
import com.examplatform.modules.evaluation.repository.EvaluationResultRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Service
@RequiredArgsConstructor
public class JsonExportService {

    private final EvaluationResultRepository resultRepository;
    private final EvaluationResultMapper resultMapper;
    private final ObjectMapper objectMapper = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .findAndRegisterModules();

    public byte[] generateJson(String runId) {
        List<ResultResponse> results = resultRepository.findByRunId(runId).stream()
                .map(resultMapper::toResponse)
                .toList();
        try {
            return objectMapper.writeValueAsString(results).getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("JSON export ব্যর্থ হয়েছে: " + e.getMessage(), e);
        }
    }
}
