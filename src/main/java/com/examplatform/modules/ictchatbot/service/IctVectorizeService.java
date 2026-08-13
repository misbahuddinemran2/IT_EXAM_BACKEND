package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.entity.IctBookChunk;
import com.examplatform.modules.ictchatbot.entity.IctOcrUpload;
import com.examplatform.modules.ictchatbot.enums.IctUploadStatus;
import com.examplatform.modules.ictchatbot.repository.IctBookChunkRepository;
import com.examplatform.modules.ictchatbot.repository.IctOcrUploadRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
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
        // TPM (Tokens Per Minute) limit-এর মধ্যে থাকতে প্রতিটা request-এর পর একটা ছোট delay দেওয়া হচ্ছে
        int savedCount = 0;
        for (String chunkText : textChunks) {
            float[] embeddingArray = embeddingService.generateEmbedding(chunkText, EmbeddingService.TASK_TYPE_DOCUMENT);

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

            sleepBetweenCalls(chunkText);
        }

        // 3. upload status আপডেট করো
        upload.setStatus(IctUploadStatus.VECTORIZED);
        uploadRepository.save(upload);

        return savedCount;
    }

    /*
     * ===================================
     * ONE-TIME MIGRATION: RE-EMBED ALL EXISTING CHUNKS
     *
     * task_type (RETRIEVAL_DOCUMENT / RETRIEVAL_QUERY) যোগ করার পর
     * আগে vectorize হওয়া chunk গুলোর embedding পুরনো (task_type ছাড়া)
     * পদ্ধতিতে তৈরি হয়ে আছে। এই মেথড শুধু embedding রিফ্রেশ করে,
     * content/chunking কিছুই পরিবর্তন করে না।
     *
     * কাজ শেষ হলে এই মেথড এবং এটাকে কল করা admin endpoint
     * নিরাপদে মুছে ফেলা যাবে।
     * ===================================
     */

    @Transactional
    public int reEmbedAllChunks() {
        List<IctBookChunk> allChunks = chunkRepository.findAll();

        int updated = 0;

        for (IctBookChunk chunk : allChunks) {
            try {
                float[] embeddingArray =
                        embeddingService.generateEmbedding(
                                chunk.getContent(),
                                EmbeddingService.TASK_TYPE_DOCUMENT
                        );

                chunk.setEmbedding(floatArrayToVectorString(embeddingArray));
                chunkRepository.save(chunk);
                updated++;

                sleepBetweenCalls(chunk.getContent());

            } catch (Exception e) {
                log.error("Re-embed failed for chunk id={}", chunk.getId(), e);
            }
        }

        log.info("Re-embed complete. {} / {} chunks updated.", updated, allChunks.size());

        return updated;
    }

    /*
     * TPM (Tokens Per Minute) বাজেট রক্ষা করার জন্য প্রতিটা embedding call-এর পর delay।
     * chunk যত বড়, delay তত বেশি — কারণ বড় chunk বেশি টোকেন consume করে।
     * বেস ডিলে ১২০০ms (RPM safety), + প্রতি ১০০ শব্দে বাড়তি ২০০ms (TPM safety margin)।
     */
    private void sleepBetweenCalls(String chunkText) {
        int wordCount = (chunkText == null || chunkText.isBlank())
                ? 0
                : chunkText.trim().split("\\s+").length;

        long baseDelayMs = 1200L;
        long extraDelayMs = (wordCount / 100) * 200L;
        long totalDelayMs = baseDelayMs + extraDelayMs;

        try {
            Thread.sleep(totalDelayMs);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Vectorize delay interrupted", ie);
        }
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
