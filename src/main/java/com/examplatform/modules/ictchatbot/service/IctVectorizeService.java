
package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.entity.IctBookChunk;
import com.examplatform.modules.ictchatbot.entity.IctOcrUpload;
import com.examplatform.modules.ictchatbot.enums.IctUploadStatus;
import com.examplatform.modules.ictchatbot.repository.IctBookChunkRepository;
import com.examplatform.modules.ictchatbot.repository.IctOcrUploadRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IctVectorizeService {

    private final IctOcrUploadRepository uploadRepository;
    private final IctBookChunkRepository chunkRepository;
    private final ChunkingService chunkingService;
    private final EmbeddingService embeddingService;

    @Transactional
    public int vectorizeUpload(String uploadId) {
        IctOcrUpload upload = uploadRepository.findById(uploadId)
                .orElseThrow(() -> new IllegalArgumentException("Upload not found: " + uploadId));

        if (upload.getStatus() != IctUploadStatus.REVIEWED) {
            throw new IllegalStateException(
                    "Upload must be REVIEWED before vectorizing. Current status: " + upload.getStatus());
        }

        // re-vectorize হলে (আগে vectorize হয়ে থাকলে, পরে আবার edit+review করে আবার vectorize করা হলে)
        // পুরনো chunk গুলো আগে মুছে ফেলা - duplicate chunk তৈরি এড়াতে
        chunkRepository.deleteBySourceUploadId(uploadId);

        // 1. টেক্সটকে chunk-এ ভাগ করো
        List<String> textChunks = chunkingService.chunkText(upload.getOcrText());

        if (textChunks.isEmpty()) {
            throw new IllegalStateException("No chunks generated from upload text");
        }

        // 2. প্রতিটা chunk-এর embedding বানাও + সেভ করো
        int savedCount = 0;
        for (String chunkText : textChunks) {
            float[] embeddingArray = embeddingService.generateEmbedding(chunkText);

            IctBookChunk chunk = IctBookChunk.builder()
                    .sourceUploadId(upload.getId())
                    .content(chunkText)
                    .writerName(upload.getWriterName())
                    .subjectId(upload.getSubjectId())
                    .chapterId(upload.getChapterId())
                    .topicId(upload.getTopicId())
                    .embedding(floatArrayToVectorString(embeddingArray))
                    .build();

            chunkRepository.save(chunk);
            savedCount++;
        }

        // 3. upload status আপডেট করো
        upload.setStatus(IctUploadStatus.VECTORIZED);
        uploadRepository.save(upload);

        return savedCount;
    }

    private String floatArrayToVectorString(float[] arr) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < arr.length; i++) {
            if (i > 0) sb.append(",");
            sb.append(arr[i]);
        }
        sb.append("]");
        return sb.toString();
    }
}
