package com.examplatform.modules.evaluation.mapper;

import com.examplatform.modules.evaluation.dto.RunResponse;
import com.examplatform.modules.evaluation.dto.RunSummaryResponse;
import com.examplatform.modules.evaluation.entity.EvaluationRun;
import org.springframework.stereotype.Component;

@Component
public class EvaluationRunMapper {

    /**
     * configuration_snapshot বানায় — profile-এর বর্তমান অবস্থার একটা immutable
     * copy, যাতে profile পরে edit হলেও এই run reproducible থাকে।
     */
    public java.util.Map<String, Object> buildConfigurationSnapshot(
            com.examplatform.modules.evaluation.entity.EvaluationProfile profile,
            com.examplatform.modules.evaluation.entity.EvaluationPrompt prompt) {
        java.util.Map<String, Object> snapshot = new java.util.LinkedHashMap<>();
        snapshot.put("model", profile.getModelName());
        snapshot.put("embeddingModel", profile.getEmbeddingModel());
        snapshot.put("topK", profile.getTopK());
        snapshot.put("threshold", profile.getSimilarityThreshold());
        snapshot.put("temperature", profile.getTemperature());
        snapshot.put("chunkStrategy", profile.getChunkStrategy());
        snapshot.put("maxTokens", profile.getMaxTokens());
        snapshot.put("promptName", prompt.getName());
        snapshot.put("promptVersion", prompt.getVersion());
        return snapshot;
    }

    public RunResponse toResponse(EvaluationRun run) {
        return RunResponse.builder()
                .id(run.getId())
                .datasetId(run.getDataset().getId())
                .datasetName(run.getDataset().getName())
                .datasetVersion(run.getDatasetVersion())
                .profileId(run.getProfile().getId())
                .profileName(run.getProfile().getName())
                .promptId(run.getPrompt().getId())
                .promptName(run.getPrompt().getName())
                .configurationSnapshot(run.getConfigurationSnapshot())
                .status(run.getStatus().name())
                .totalQuestions(run.getTotalQuestions())
                .processedQuestions(run.getProcessedQuestions())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .triggeredByAdminId(run.getTriggeredByAdmin() != null ? run.getTriggeredByAdmin().getId() : null)
                .createdAt(run.getCreatedAt())
                .updatedAt(run.getUpdatedAt())
                .build();
    }

    public RunSummaryResponse toSummaryResponse(EvaluationRun run) {
        return RunSummaryResponse.builder()
                .id(run.getId())
                .datasetName(run.getDataset().getName())
                .profileName(run.getProfile().getName())
                .modelName(run.getProfile().getModelName())
                .status(run.getStatus().name())
                .totalQuestions(run.getTotalQuestions())
                .processedQuestions(run.getProcessedQuestions())
                .startedAt(run.getStartedAt())
                .completedAt(run.getCompletedAt())
                .build();
    }
}
