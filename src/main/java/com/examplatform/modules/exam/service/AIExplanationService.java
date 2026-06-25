//package com.examplatform.modules.exam.service;
//
//import com.examplatform.modules.exam.dto.AIExplanationRequest;
//import com.examplatform.modules.exam.dto.AIExplanationResponse;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.stereotype.Service;
//import java.util.Arrays;
//import java.util.List;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//public class AIExplanationService {
//
//    @Value("${anthropic.api.key}")
//    private String apiKey;
//
//    @Cacheable(value = "explanations", key = "#request.questionId")
//    public AIExplanationResponse generateExplanation(AIExplanationRequest request) {
//        try {
//            String prompt = buildPrompt(request);
//            String response = callClaudeAPI(prompt);
//            return parseResponse(request.getQuestionId(), response);
//        } catch (Exception e) {
//            log.error("Error generating explanation", e);
//            return AIExplanationResponse.builder()
//                    .questionId(request.getQuestionId())
//                    .briefExplanation("উত্তর প্রক্রিয়াকরণ হচ্ছে...")
//                    .detailedExplanation("")
//                    .keyPoints(Arrays.asList())
//                    .build();
//        }
//    }
//
//    private String buildPrompt(AIExplanationRequest request) {
//        String language = "bn".equals(request.getLanguage())
//                ? "বাংলা"
//                : "English";
//
//        return """
//            প্রশ্ন: %s
//
//            নির্বাচিত উত্তর: %s
//            সঠিক উত্তর: %s
//
//            ভাষা: %s
//
//            নিম্নলিখিত ফরম্যাটে উত্তর দিন:
//
//            সংক্ষিপ্ত ব্যাখ্যা: [2-3 লাইন]
//
//            বিস্তারিত ব্যাখ্যা: [বিস্তারিত উত্তর]
//
//            মূল বিষয়গুলি:
//            - বিষয় 1
//            - বিষয় 2
//            - বিষয় 3
//
//            স্মৃতি মনে রাখার কৌশল: [এটি মনে রাখার একটি কৌশল]
//
//            সম্পর্কিত বিষয়গুলি:
//            - বিষয় A
//            - বিষয় B
//            """.formatted(request.getQuestionText(),
//                request.getSelectedAnswerId(),
//                request.getCorrectAnswerId(),
//                language);
//    }
//
//    private String callClaudeAPI(String prompt) {
//        // এই implementation এ Claude API call করবে
//        // For now, dummy response
//        return """
//            সংক্ষিপ্ত ব্যাখ্যা: প্রোটিন হল অ্যামিনো এসিডের একটি পলিমার যা পেপটাইড বন্ডের মাধ্যমে সংযুক্ত।
//
//            বিস্তারিত ব্যাখ্যা: প্রোটিন জীবন্ত জীবের জন্য অত্যন্ত গুরুত্বপূর্ণ জৈব যৌগ। এটি 20 ধরনের অ্যামিনো এসিড নিয়ে গঠিত।
//
//            মূল বিষয়গুলি:
//            - 20 ধরনের অ্যামিনো এসিড বিদ্যমান
//            - পেপটাইড বন্ড কার্বোক্সিল গ্রুপ এবং অ্যামিনো গ্রুপের মধ্যে গঠিত হয়
//            - প্রোটিন তিন ধরনের: ফাইব্রাস, গ্লোবুলার এবং কনজুগেটেড
//
//            স্মৃতি মনে রাখার কৌশল: P.A.C = Protein = Amino acids Chained
//
//            সম্পর্কিত বিষয়গুলি:
//            - Amino acids
//            - Peptide bonds
//            - Protein structure
//            """;
//    }
//
//    private AIExplanationResponse parseResponse(String questionId, String response) {
//        // Response থেকে sections extract করুন
//        String briefExplanation = extractSection(response, "সংক্ষিপ্ত ব্যাখ্যা:");
//        String detailedExplanation = extractSection(response, "বিস্তারিত ব্যাখ্যা:");
//        List<String> keyPoints = extractPoints(response, "মূল বিষয়গুলি:");
//        String mnemonicTrick = extractSection(response, "স্মৃতি মনে রাখার কৌশল:");
//        List<String> relatedTopics = extractPoints(response, "সম্পর্কিত বিষয়গুলি:");
//
//        return AIExplanationResponse.builder()
//                .questionId(questionId)
//                .briefExplanation(briefExplanation)
//                .detailedExplanation(detailedExplanation)
//                .keyPoints(keyPoints)
//                .mnemonicTrick(mnemonicTrick)
//                .relatedTopics(relatedTopics)
//                .build();
//    }
//
//    private String extractSection(String response, String sectionTitle) {
//        int startIndex = response.indexOf(sectionTitle);
//        if (startIndex == -1) return "";
//
//        startIndex += sectionTitle.length();
//        int endIndex = response.indexOf("\n\n", startIndex);
//        if (endIndex == -1) endIndex = response.length();
//
//        return response.substring(startIndex, endIndex).trim();
//    }
//
//    private List<String> extractPoints(String response, String sectionTitle) {
//        String section = extractSection(response, sectionTitle);
//        return Arrays.stream(section.split("\n"))
//                .filter(line -> line.trim().startsWith("-"))
//                .map(line -> line.replaceFirst("^\\s*-\\s*", "").trim())
//                .filter(line -> !line.isEmpty())
//                .limit(5)
//                .toList();
//    }
//}