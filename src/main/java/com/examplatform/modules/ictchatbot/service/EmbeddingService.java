package com.examplatform.modules.ictchatbot.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${gemini.api.key}")
    private String apiKey;

    // এমবেডিং মডেল টেক্সট জেনারেশন মডেল থেকে আলাদা — এটা ৭৬৮-dimension ভেক্টর দেয়
    private static final String EMBEDDING_MODEL = "text-embedding-004";

    public float[] generateEmbedding(String text) {
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

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(requestBody.toString(), headers);

        try {
            String response = restTemplate.postForObject(url, request, String.class);
            return extractEmbedding(response);
        } catch (Exception e) {
            throw new RuntimeException("Gemini embedding call failed: " + e.getMessage(), e);
        }
    }

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
        return result;
    }
}
