package com.examplatform.modules.ictchatbot.service;

import com.examplatform.modules.ictchatbot.dto.IctAskResponse;
import com.examplatform.modules.ictchatbot.entity.IctAnswerCache;
import com.examplatform.modules.ictchatbot.entity.IctBookChunk;
import com.examplatform.modules.ictchatbot.repository.IctAnswerCacheRepository;
import com.examplatform.modules.ictchatbot.repository.IctBookChunkRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class IctAskService {

    private final IctAnswerCacheRepository cacheRepository;
    private final IctBookChunkRepository chunkRepository;
    private final EmbeddingService embeddingService;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model:gemini-3.1-flash-lite}")
    private String model;

    private static final double CACHE_DISTANCE_THRESHOLD = 0.05; // similarity > 0.95
    private static final int TOP_K = 5;

    public IctAskResponse ask(String question) {

        // 1. প্রশ্নের embedding বানাও
        float[] questionEmbeddingArray = embeddingService.generateEmbedding(question);
        String questionEmbeddingStr = floatArrayToVectorString(questionEmbeddingArray);

        // 2. cache চেক করো
        List<IctAnswerCache> cacheHits = cacheRepository.findClosestMatch(questionEmbeddingStr, CACHE_DISTANCE_THRESHOLD);
        if (!cacheHits.isEmpty()) {
            IctAnswerCache hit = cacheHits.get(0);
            hit.setHitCount(hit.getHitCount() + 1);
            cacheRepository.save(hit);

            return IctAskResponse.builder()
                    .answer(hit.getCachedAnswer())
                    .sourceWriters(List.of())
                    .diagramUrls(List.of())
                    .fromCache(true)
                    .build();
        }

        // 3. vector search করো — সব লেখক জুড়ে (writerName = null মানে filter নেই)
        List<IctBookChunk> similarChunks = chunkRepository.findSimilarChunks(questionEmbeddingStr, null, TOP_K);

        if (similarChunks.isEmpty()) {
            return IctAskResponse.builder()
                    .answer("দুঃখিত, এই প্রশ্নের উত্তর বইয়ের কনটেন্টে পাওয়া যায়নি।")
                    .sourceWriters(List.of())
                    .diagramUrls(List.of())
                    .fromCache(false)
                    .build();
        }

        // 4. Gemini দিয়ে answer generate করো (লেখকের নাম prompt-এ পাঠানো হচ্ছে না — hallucinated attribution এড়াতে)
        String answer = generateAnswerFromChunks(question, similarChunks);

        // 5. source writer + diagram metadata আলাদাভাবে সংগ্রহ করো (Gemini-নির্ভর না, সরাসরি DB থেকে)
        List<String> sourceWriters = similarChunks.stream()
                .map(IctBookChunk::getWriterName)
                .distinct()
                .collect(Collectors.toList());

        List<String> diagramUrls = similarChunks.stream()
                .map(IctBookChunk::getDiagramUrl)
                .filter(url -> url != null && !url.isBlank())
                .distinct()
                .collect(Collectors.toList());

        // 6. cache-এ সেভ করো
        IctAnswerCache cacheEntry = IctAnswerCache.builder()
                .questionText(question)
                .questionEmbedding(questionEmbeddingStr)
                .cachedAnswer(answer)
                .build();
        cacheRepository.save(cacheEntry);

        return IctAskResponse.builder()
                .answer(answer)
                .sourceWriters(sourceWriters)
                .diagramUrls(diagramUrls)
                .fromCache(false)
                .build();
    }

    private String generateAnswerFromChunks(String question, List<IctBookChunk> chunks) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model + ":generateContent?key=" + apiKey;

        StringBuilder contextBuilder = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            contextBuilder.append("অংশ ").append(i + 1).append(":\n")
                    .append(chunks.get(i).getContent()).append("\n\n");
        }

        // লেখকের নাম ইচ্ছাকৃতভাবে prompt-এ দেওয়া হচ্ছে না — attribution আলাদাভাবে metadata থেকে দেখানো হবে
        String prompt = "তুমি একজন HSC ICT শিক্ষক। নিচের বইয়ের অংশগুলো থেকে শুধুমাত্র প্রাসঙ্গিক তথ্য ব্যবহার করে "
                + "ছাত্রের প্রশ্নের উত্তর বাংলায় দাও। বইয়ের বাইরের কোনো তথ্য যোগ করবে না। "
                + "একাধিক অংশ থেকে তথ্য মিলিয়ে একটা সুসংগঠিত উত্তর তৈরি করো। "
                + "যদি উত্তর এই অংশগুলোতে না থাকে, তাহলে বলো যে এই তথ্য বইয়ে পাওয়া যায়নি।\n\n"
                + "বইয়ের অংশ:\n" + contextBuilder
                + "\nছাত্রের প্রশ্ন: " + question
                + "\n\nউত্তর সহজ, স্পষ্ট ভাষায় দাও।";

        var partsArray = objectMapper.createArrayNode();
        var textPart = objectMapper.createObjectNode();
        textPart.put("text", prompt);
        partsArray.add(textPart);

        var contentNode = objectMapper.createObjectNode();
        contentNode.set("parts", partsArray);

        var contentsArray = objectMapper.createArrayNode();
        contentsArray.add(contentNode);

        var requestBody = objectMapper.createObjectNode();
        requestBody.set("contents", contentsArray);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            return extractText(response);
        } catch (Exception e) {
            throw new RuntimeException("Gemini answer generation failed: " + e.getMessage(), e);
        }
    }

    private String extractText(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode textNode = root
                .path("candidates").get(0)
                .path("content")
                .path("parts").get(0)
                .path("text");

        if (textNode.isMissingNode()) {
            throw new IllegalStateException("Gemini response থেকে text পাওয়া যায়নি: " + responseJson);
        }
        return textNode.asText().trim();
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
