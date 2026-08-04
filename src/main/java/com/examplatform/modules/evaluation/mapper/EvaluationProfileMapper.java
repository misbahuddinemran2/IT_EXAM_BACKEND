package com.examplatform.modules.evaluation.mapper;

import com.examplatform.modules.evaluation.dto.ProfileCreateRequest;
import com.examplatform.modules.evaluation.dto.ProfileResponse;
import com.examplatform.modules.evaluation.dto.ProfileUpdateRequest;
import com.examplatform.modules.evaluation.entity.EvaluationProfile;
import org.springframework.stereotype.Component;

@Component
public class EvaluationProfileMapper {

    public EvaluationProfile toEntity(ProfileCreateRequest req) {
        return EvaluationProfile.builder()
                .name(req.getName())
                .modelName(req.getModelName())
                .embeddingModel(req.getEmbeddingModel())
                .topK(req.getTopK() != null ? req.getTopK() : 5)
                .similarityThreshold(req.getSimilarityThreshold())
                .temperature(req.getTemperature())
                .chunkStrategy(req.getChunkStrategy())
                .maxTokens(req.getMaxTokens())
                .build();
    }

    public void applyUpdate(EvaluationProfile profile, ProfileUpdateRequest req) {
        if (req.getName() != null) profile.setName(req.getName());
        if (req.getModelName() != null) profile.setModelName(req.getModelName());
        if (req.getEmbeddingModel() != null) profile.setEmbeddingModel(req.getEmbeddingModel());
        if (req.getTopK() != null) profile.setTopK(req.getTopK());
        if (req.getSimilarityThreshold() != null) profile.setSimilarityThreshold(req.getSimilarityThreshold());
        if (req.getTemperature() != null) profile.setTemperature(req.getTemperature());
        if (req.getChunkStrategy() != null) profile.setChunkStrategy(req.getChunkStrategy());
        if (req.getMaxTokens() != null) profile.setMaxTokens(req.getMaxTokens());
    }

    public ProfileResponse toResponse(EvaluationProfile profile) {
        return ProfileResponse.builder()
                .id(profile.getId())
                .name(profile.getName())
                .modelName(profile.getModelName())
                .embeddingModel(profile.getEmbeddingModel())
                .topK(profile.getTopK())
                .similarityThreshold(profile.getSimilarityThreshold())
                .temperature(profile.getTemperature())
                .chunkStrategy(profile.getChunkStrategy())
                .maxTokens(profile.getMaxTokens())
                .createdAt(profile.getCreatedAt())
                .updatedAt(profile.getUpdatedAt())
                .build();
    }
}
