package com.examplatform.modules.evaluation.mapper;

import com.examplatform.modules.auth.repository.AdminUserRepository;
import com.examplatform.modules.evaluation.dto.DatasetCreateRequest;
import com.examplatform.modules.evaluation.dto.DatasetResponse;
import com.examplatform.modules.evaluation.dto.DatasetSummaryResponse;
import com.examplatform.modules.evaluation.dto.DatasetUpdateRequest;
import com.examplatform.modules.evaluation.entity.EvaluationDataset;
import com.examplatform.modules.evaluation.enums.DatasetDomain;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.NoSuchElementException;

@Component
@RequiredArgsConstructor
public class EvaluationDatasetMapper {

    private final AdminUserRepository adminUserRepository;

    public EvaluationDataset toEntity(DatasetCreateRequest req, String adminId) {
        return EvaluationDataset.builder()
                .name(req.getName())
                .domain(req.getDomain() != null ? DatasetDomain.valueOf(req.getDomain()) : DatasetDomain.HSC_ICT)
                .language(req.getLanguage() != null ? req.getLanguage() : "bn")
                .description(req.getDescription())
                .createdByAdmin(adminId != null ? adminUserRepository.findById(adminId)
                        .orElseThrow(() -> new NoSuchElementException("Admin not found: " + adminId)) : null)
                .build();
    }

    /**
     * Partial update — request এ যেই field null না, সেটাই update হবে।
     */
    public void applyUpdate(EvaluationDataset dataset, DatasetUpdateRequest req) {
        if (req.getName() != null) dataset.setName(req.getName());
        if (req.getDomain() != null) dataset.setDomain(DatasetDomain.valueOf(req.getDomain()));
        if (req.getLanguage() != null) dataset.setLanguage(req.getLanguage());
        if (req.getDescription() != null) dataset.setDescription(req.getDescription());
    }

    public DatasetResponse toResponse(EvaluationDataset dataset) {
        return DatasetResponse.builder()
                .id(dataset.getId())
                .name(dataset.getName())
                .domain(dataset.getDomain().name())
                .language(dataset.getLanguage())
                .description(dataset.getDescription())
                .createdByAdminId(dataset.getCreatedByAdmin() != null ? dataset.getCreatedByAdmin().getId() : null)
                .createdByAdminName(dataset.getCreatedByAdmin() != null ? dataset.getCreatedByAdmin().getUsername() : null)
                .createdAt(dataset.getCreatedAt())
                .updatedAt(dataset.getUpdatedAt())
                .build();
    }

    public DatasetSummaryResponse toSummaryResponse(EvaluationDataset dataset, long questionCount, long runCount) {
        return DatasetSummaryResponse.builder()
                .id(dataset.getId())
                .name(dataset.getName())
                .domain(dataset.getDomain().name())
                .language(dataset.getLanguage())
                .questionCount(questionCount)
                .runCount(runCount)
                .build();
    }
}
