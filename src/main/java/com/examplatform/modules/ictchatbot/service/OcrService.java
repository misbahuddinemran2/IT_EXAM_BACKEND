package com.examplatform.modules.ictchatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OcrService {

    @Value("${ocr.space.api.key}")
    private String ocrApiKey;

    private static final String OCR_SPACE_URL = "https://api.ocr.space/parse/image";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public String extractTextFromImageUrl(String imageUrl) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("apikey", ocrApiKey);
        body.add("url", imageUrl);
        body.add("language", "bng"); // বাংলার জন্য
        body.add("OCREngine", "3");  // Engine 2 - বাংলার জন্য তুলনামূলক ভালো

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(OCR_SPACE_URL, request, String.class);

            JsonNode root = objectMapper.readTree(response.getBody());

            if (root.path("IsErroredOnProcessing").asBoolean(false)) {
                String errorMsg = root.path("ErrorMessage").toString();
                throw new RuntimeException("OCR.space error: " + errorMsg);
            }

            JsonNode parsedResults = root.path("ParsedResults");
            if (parsedResults.isArray() && parsedResults.size() > 0) {
                return parsedResults.get(0).path("ParsedText").asText("");
            }

            return "";

        } catch (Exception e) {
            throw new RuntimeException("OCR processing failed: " + e.getMessage(), e);
        }
    }
}
