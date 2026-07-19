package com.examplatform.modules.ictchatbot.service;

/*
 * ===================================
 * QUICK REPLY SIMILARITY UTIL
 *
 * Pure Java string similarity — কোনো external
 * dependency লাগে না।
 *
 * ব্যবহার হয়:
 * - Jaro-Winkler → character-level similarity
 *   (spelling variation/ছোট typo ধরার জন্য)
 * - Jaccard → token-level similarity
 *   (common word overlap ধরার জন্য)
 * ===================================
 */
public final class QuickReplySimilarityUtil {

    private QuickReplySimilarityUtil() {
        // utility class, instantiate করা যাবে না
    }


    /*
     * ===================================
     * JARO-WINKLER SIMILARITY
     *
     * Return: 0.0 (কোনো মিল নেই) থেকে 1.0 (সম্পূর্ণ মিল)
     * ===================================
     */
    public static double jaroWinkler(String s1, String s2) {

        if (s1 == null || s2 == null) {
            return 0.0;
        }

        if (s1.equals(s2)) {
            return 1.0;
        }

        double jaro = jaroSimilarity(s1, s2);

        // Winkler adjustment: common prefix (max 4 char) বেশি weight পায়
        int prefixLength = commonPrefixLength(s1, s2, 4);

        double winklerScale = 0.1;

        return jaro + (prefixLength * winklerScale * (1 - jaro));
    }


    private static double jaroSimilarity(String s1, String s2) {

        int len1 = s1.length();
        int len2 = s2.length();

        if (len1 == 0 || len2 == 0) {
            return 0.0;
        }

        int matchDistance = Math.max(len1, len2) / 2 - 1;
        if (matchDistance < 0) {
            matchDistance = 0;
        }

        boolean[] s1Matches = new boolean[len1];
        boolean[] s2Matches = new boolean[len2];

        int matches = 0;

        for (int i = 0; i < len1; i++) {

            int start = Math.max(0, i - matchDistance);
            int end = Math.min(i + matchDistance + 1, len2);

            for (int j = start; j < end; j++) {

                if (s2Matches[j]) {
                    continue;
                }

                if (s1.charAt(i) != s2.charAt(j)) {
                    continue;
                }

                s1Matches[i] = true;
                s2Matches[j] = true;
                matches++;
                break;
            }
        }

        if (matches == 0) {
            return 0.0;
        }

        // transpositions গণনা
        double transpositions = 0;
        int k = 0;

        for (int i = 0; i < len1; i++) {

            if (!s1Matches[i]) {
                continue;
            }

            while (!s2Matches[k]) {
                k++;
            }

            if (s1.charAt(i) != s2.charAt(k)) {
                transpositions++;
            }

            k++;
        }

        transpositions /= 2;

        return ((matches / (double) len1)
                + (matches / (double) len2)
                + ((matches - transpositions) / matches)) / 3.0;
    }


    private static int commonPrefixLength(String s1, String s2, int maxLength) {

        int limit = Math.min(maxLength, Math.min(s1.length(), s2.length()));

        int i = 0;

        while (i < limit && s1.charAt(i) == s2.charAt(i)) {
            i++;
        }

        return i;
    }


    /*
     * ===================================
     * JACCARD SIMILARITY (token-based)
     *
     * প্রশ্নকে শব্দে ভেঙে (space দিয়ে split)
     * common word / total unique word হিসাব করে।
     *
     * Return: 0.0 থেকে 1.0
     * ===================================
     */
    public static double jaccard(String s1, String s2) {

        if (s1 == null || s2 == null || s1.isBlank() || s2.isBlank()) {
            return 0.0;
        }

        var tokens1 = java.util.Set.of(s1.trim().split("\\s+"));
        var tokens2 = java.util.Set.of(s2.trim().split("\\s+"));

        var union = new java.util.HashSet<>(tokens1);
        union.addAll(tokens2);

        var intersection = new java.util.HashSet<>(tokens1);
        intersection.retainAll(tokens2);

        if (union.isEmpty()) {
            return 0.0;
        }

        return intersection.size() / (double) union.size();
    }


    /*
     * ===================================
     * COMBINED TEXT SIMILARITY
     *
     * Jaro-Winkler (character) + Jaccard (token)
     * এর weighted average।
     * ===================================
     */
    public static double combinedSimilarity(String s1, String s2) {

        double jw = jaroWinkler(s1, s2);
        double jaccardScore = jaccard(s1, s2);

        return (jw * 0.6) + (jaccardScore * 0.4);
    }
}
