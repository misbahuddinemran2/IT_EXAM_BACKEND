package com.examplatform.modules.ictchatbot.service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;

/*
 * ===================================
 * ICT INTENT DETECTOR
 *
 * Normalized প্রশ্ন থেকে local keyword matching
 * দিয়ে intent বের করে। কোনো API/ML model লাগে না।
 *
 * IMPORTANT: LinkedHashMap ব্যবহার করা হয়েছে যাতে
 * insertion order বজায় থাকে — বেশি specific intent
 * আগে চেক করা যায় (যেমন FEATURES আগে,
 * DEFINITION পরে, কারণ "বৈশিষ্ট্য কী" এ দুটোই
 * থাকতে পারে কিন্তু FEATURES ই আসল intent)
 * ===================================
 */
public final class IctIntentDetector {

    private IctIntentDetector() {
    }


    public enum Intent {
        FEATURES,
        ADVANTAGE,
        DISADVANTAGE,
        APPLICATION,
        EXAMPLE,
        COMPARISON,
        PROCESS,
        IMPORTANCE,
        DEFINITION,   // সবচেয়ে generic, তাই সবার শেষে চেক হবে
        UNKNOWN
    }


    private static final Map<Intent, List<String>> INTENT_KEYWORDS =
            new LinkedHashMap<>();

    static {

        INTENT_KEYWORDS.put(Intent.FEATURES, List.of(
                "বৈশিষ্ট্য", "বৈশিষ্ট্যসমূহ", "গুণ", "ধর্ম", "boisisto"
        ));

        INTENT_KEYWORDS.put(Intent.ADVANTAGE, List.of(
                "সুবিধা", "উপকারিতা", "লাভ", "সুফল"
        ));

        INTENT_KEYWORDS.put(Intent.DISADVANTAGE, List.of(
                "অসুবিধা", "ক্ষতি", "সমস্যা", "কুফল"
        ));

        INTENT_KEYWORDS.put(Intent.APPLICATION, List.of(
                "ব্যবহার", "প্রয়োগ", "কোথায় ব্যবহৃত", "ব্যবহৃত হয়"
        ));

        INTENT_KEYWORDS.put(Intent.EXAMPLE, List.of(
                "উদাহরণ", "example", "উদাহরণসহ"
        ));

        INTENT_KEYWORDS.put(Intent.COMPARISON, List.of(
                "পার্থক্য", "তুলনা", "differences", "compare"
        ));

        INTENT_KEYWORDS.put(Intent.PROCESS, List.of(
                "প্রক্রিয়া", "ধাপ", "কিভাবে", "কীভাবে"
        ));

        INTENT_KEYWORDS.put(Intent.IMPORTANCE, List.of(
                "গুরুত্ব", "প্রয়োজনীয়তা", "কেন দরকার"
        ));

        INTENT_KEYWORDS.put(Intent.DEFINITION, List.of(
                "কী", "কি", "সংজ্ঞা", "বলতে কী বোঝায়", "বুঝ", "কাকে বলে"
        ));
    }


    /*
     * ===================================
     * DETECT
     *
     * normalized (lowercase, punctuation-free) প্রশ্ন
     * প্যারামিটার হিসেবে নেয়।
     *
     * প্রথম যেই intent এর কোনো keyword মিলবে,
     * সেটাই return হবে (LinkedHashMap order অনুযায়ী
     * specific intent আগে চেক হয়)।
     * ===================================
     */
    public static Intent detect(String normalizedQuestion) {

        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return Intent.UNKNOWN;
        }

        for (Map.Entry<Intent, List<String>> entry : INTENT_KEYWORDS.entrySet()) {

            for (String keyword : entry.getValue()) {

                if (normalizedQuestion.contains(keyword)) {
                    return entry.getKey();
                }
            }
        }

        return Intent.UNKNOWN;
    }


    /*
     * ===================================
     * EXTRACT TOPIC
     *
     * Intent keyword গুলো বাদ দিয়ে বাকি অংশটাই
     * topic হিসেবে ধরা হয়।
     *
     * Example:
     * "iot এর সুবিধা কী" → intent keyword ("সুবিধা", "কী")
     * বাদ দিলে বাকি থাকে "iot এর" → trim করে "iot এর"
     * ===================================
     */
    public static String extractTopic(String normalizedQuestion, Intent intent) {

        if (normalizedQuestion == null || normalizedQuestion.isBlank()) {
            return "";
        }

        String result = normalizedQuestion;

        if (intent != Intent.UNKNOWN) {

            List<String> keywords = INTENT_KEYWORDS.get(intent);

            if (keywords != null) {

                for (String keyword : keywords) {
                    result = result.replace(keyword, " ");
                }
            }
        }

        // সাধারণ filler word/connector বাদ দেওয়া
        result = result
                .replaceAll("\\b(এর|কি|কী|দাও|বল|বলো)\\b", " ")
                .replaceAll("\\s+", " ")
                .trim();

        return result;
    }
}
