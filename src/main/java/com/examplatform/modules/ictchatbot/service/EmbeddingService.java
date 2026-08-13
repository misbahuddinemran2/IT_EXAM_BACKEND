package com.examplatform.modules.ictchatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    // এমবেডিং মডেল টেক্সট জেনারেশন মডেল থেকে আলাদা — এটা ৭৬৮-dimension ভেক্টর দেয়
    private static final String EMBEDDING_MODEL = "gemini-embedding-001";

    // Retrieval task type constants
    public static final String TASK_TYPE_DOCUMENT = "RETRIEVAL_DOCUMENT";
    public static final String TASK_TYPE_QUERY = "RETRIEVAL_QUERY";

    // পুরনো কোথাও থেকে থাকলে ভেঙে না গিয়ে ডিফল্টভাবে QUERY টাইপ ব্যবহার করবে
    public float[] generateEmbedding(String text) {
        return generateEmbedding(text, TASK_TYPE_QUERY);
    }

    public float[] generateEmbedding(String text, String taskType) {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + EMBEDDING_MODEL + ":embedContent?key=" + apiKey;

        var contentNode = objectMapper.createObjectNode();
        var partsArray = objectMapper.createArrayNode();
        var textPart = objectMapper.createObjectNode();
        textPart.put("text", text);
        partsArray.add(textPart);
        contentNode.set("parts", partsArray);

        var requestBody = objectMapper.createObjectNode();
        requestBody.put("model", "models/" + EMBEDDING_MODEL);
        requestBody.set("content", contentNode);
        requestBody.put("outputDimensionality", 768);
        requestBody.put("taskType", taskType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        int maxRetries = 5;
        long baseDelayMs = 2000L; // 2s, 4s, 8s, 16s, 32s ... exponential backoff

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                String response = restTemplate.postForObject(url, request, String.class);
                return extractEmbedding(response);
            } catch (HttpClientErrorException.TooManyRequests e) {
                if (attempt == maxRetries) {
                    throw new RuntimeException(
                            "Gemini embedding call failed after " + maxRetries + " retries (429 TPM/RPM limit): "
                                    + e.getMessage(), e);
                }
                long delay = baseDelayMs * (1L << attempt); // exponential backoff
                log.warn("Gemini 429 (rate limit) পেয়েছি, attempt={}/{}, {}ms পরে retry করছি",
                        attempt + 1, maxRetries, delay);
                sleep(delay);
            } catch (Exception e) {
                throw new RuntimeException("Gemini embedding call failed: " + e.getMessage(), e);
            }
        }

        // এখানে কখনো পৌঁছানো উচিত না, কিন্তু compiler-এর জন্য
        throw new RuntimeException("Gemini embedding call failed: retries exhausted");
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Embedding retry delay interrupted", ie);
        }
    }

    private static final int EXPECTED_DIMENSION = 768;

    private float[] extractEmbedding(String responseJson) throws Exception {
        JsonNode root = objectMapper.readTree(responseJson);
        JsonNode valuesNode = root.path("embedding").path("values");

        if (valuesNode.isMissingNode() || !valuesNode.isArray()) {
            throw new IllegalStateException("Gemini response থেকে embedding পাওয়া যায়নি: " + responseJson);
        }

        List<Float> values = new ArrayList<>();
        for (JsonNode v : valuesNode) {
            values.add((float) v.asDouble());
        }

        float[] result = new float[values.size()];
        for (int i = 0; i < values.size(); i++) {
            result[i] = values.get(i);
        }

        // ===================================
        // DEFENSIVE VALIDATION
        // এই check না থাকলে malformed vector (NaN/Infinity/ভুল dimension)
        // চুপচাপ DB-তে চলে যেতে পারে এবং pgvector query silently 0 rows
        // রিটার্ন করতে পারে (exception ছাড়াই) — যেটা খুঁজে বের করা কঠিন।
        // ===================================
        if (result.length != EXPECTED_DIMENSION) {
            log.error("Embedding dimension mismatch! Expected={}, Got={}. ResponseSnippet={}",
                    EXPECTED_DIMENSION, result.length,
                    responseJson.length() > 300 ? responseJson.substring(0, 300) : responseJson);
            throw new IllegalStateException(
                    "Embedding dimension mismatch: expected " + EXPECTED_DIMENSION + " but got " + result.length);
        }

        for (int i = 0; i < result.length; i++) {
            if (Float.isNaN(result[i]) || Float.isInfinite(result[i])) {
                log.error("Embedding contains invalid value (NaN/Infinite) at index={}", i);
                throw new IllegalStateException(
                        "Embedding contains NaN/Infinite value at index " + i + " — cannot safely store/query this vector");
            }
        }

        return result;
    }
}
