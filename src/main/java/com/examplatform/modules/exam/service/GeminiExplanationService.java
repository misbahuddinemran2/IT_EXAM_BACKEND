package com.examplatform.modules.exam.service;

import com.examplatform.modules.exam.dto.AIExplanationRequest;
import com.examplatform.modules.exam.dto.AIExplanationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class GeminiExplanationService {

    @Value("${groq.api.key:test-key}")
    private String apiKey;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Cacheable(value = "explanations", key = "#request.questionId")
    public AIExplanationResponse generateExplanation(AIExplanationRequest request) {
        try {
            String response = callGroqAPI(request);
            return parseResponse(request.getQuestionId(), response);
        } catch (Exception e) {
            log.error("Error generating explanation from Groq", e);
            return getDefaultExplanation(request.getQuestionId());
        }
    }

    private String callGroqAPI(AIExplanationRequest request) {
        String url = "https://api.groq.com/openai/v1/chat/completions";

        String prompt = buildPrompt(request);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + apiKey);

        String requestBody = """
            {
              "model": "llama-3.1-8b-instant",
              "messages": [{"role": "user", "content": "%s"}],
              "max_tokens": 1000
            }
            """.formatted(prompt.replace("\"", "\\\"").replace("\n", "\\n"));

        try {
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            String response = restTemplate.postForObject(url, entity, String.class);
            return extractTextFromGroqResponse(response);
        } catch (Exception e) {
            log.error("Groq API call failed", e);
            return getDummyResponse();
        }
    }

    private String extractTextFromGroqResponse(String response) {
        try {
            Map<String, Object> jsonResponse = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> choices = (List<Map<String, Object>>) jsonResponse.get("choices");
            if (choices != null && !choices.isEmpty()) {
                Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        } catch (Exception e) {
            log.error("Failed to parse Groq response", e);
        }
        return getDummyResponse();
    }

    private String buildPrompt(AIExplanationRequest request) {
        String language = "bn".equals(request.getLanguage()) ? "বাংলা" : "English";

        return """
            প্রশ্ন: %s
            
            নির্বাচিত উত্তর: %s
            সঠিক উত্তর: %s
            
            ভাষা: %s
            
            নিম্নলিখিত ফরম্যাটে উত্তর দিন:
            
            সংক্ষিপ্ত ব্যাখ্যা: [2-3 লাইন]
            বিস্তারিত ব্যাখ্যা: [বিস্তারিত উত্তর]
            মূল বিষয়গুলি:
            - বিষয় 1
            - বিষয় 2
            - বিষয় 3
            স্মৃতি মনে রাখার কৌশল: [কৌশল]
            সম্পর্কিত বিষয়গুলি:
            - বিষয় A
            - বিষয় B
            """.formatted(request.getQuestionText(),
                request.getSelectedAnswerId(),
                request.getCorrectAnswerId(),
                language);
    }

    private String getDummyResponse() {
        return """
            সংক্ষিপ্ত ব্যাখ্যা: এটি সঠিক উত্তরের ব্যাখ্যা।
            
            বিস্তারিত ব্যাখ্যা: এই বিষয়টি আরও গভীর জ্ঞানের জন্য গুরুত্বপূর্ণ।
            
            মূল বিষয়গুলি:
            - প্রথম পয়েন্ট
            - দ্বিতীয় পয়েন্ট
            - তৃতীয় পয়েন্ট
            
            স্মৃতি মনে রাখার কৌশল: একটি সহজ কৌশল দিয়ে মনে রাখুন।
            
            সম্পর্কিত বিষয়গুলি:
            - সম্পর্কিত টপিক 1
            - সম্পর্কিত টপিক 2
            """;
    }

    private AIExplanationResponse parseResponse(String questionId, String response) {
        String briefExplanation = extractSection(response, "সংক্ষিপ্ত ব্যাখ্যা:");
        String detailedExplanation = extractSection(response, "বিস্তারিত ব্যাখ্যা:");
        List<String> keyPoints = extractPoints(response, "মূল বিষয়গুলি:");
        String mnemonicTrick = extractSection(response, "স্মৃতি মনে রাখার কৌশল:");
        List<String> relatedTopics = extractPoints(response, "সম্পর্কিত বিষয়গুলি:");

        return AIExplanationResponse.builder()
                .questionId(questionId)
                .briefExplanation(briefExplanation.isEmpty() ? "উত্তর লোড হচ্ছে..." : briefExplanation)
                .detailedExplanation(detailedExplanation)
                .keyPoints(keyPoints.isEmpty() ? Arrays.asList("আরও শিখুন") : keyPoints)
                .mnemonicTrick(mnemonicTrick)
                .relatedTopics(relatedTopics)
                .build();
    }

    private AIExplanationResponse getDefaultExplanation(String questionId) {
        return AIExplanationResponse.builder()
                .questionId(questionId)
                .briefExplanation("AI explanation লোড হচ্ছে...")
                .detailedExplanation("")
                .keyPoints(Arrays.asList())
                .build();
    }

    private String extractSection(String response, String sectionTitle) {
        int startIndex = response.indexOf(sectionTitle);
        if (startIndex == -1) return "";

        startIndex += sectionTitle.length();
        int endIndex = response.indexOf("\n\n", startIndex);
        if (endIndex == -1) endIndex = response.length();

        return response.substring(startIndex, endIndex).trim();
    }

    private List<String> extractPoints(String response, String sectionTitle) {
        String section = extractSection(response, sectionTitle);
        return Arrays.stream(section.split("\n"))
                .filter(line -> line.trim().startsWith("-"))
                .map(line -> line.replaceFirst("^\\s*-\\s*", "").trim())
                .filter(line -> !line.isEmpty())
                .limit(5)
                .toList();
    }
}