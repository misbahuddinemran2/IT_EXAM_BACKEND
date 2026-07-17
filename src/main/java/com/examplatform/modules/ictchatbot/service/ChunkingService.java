package com.examplatform.modules.ictchatbot.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int MIN_WORDS = 50;   // এর কম হলে পরের প্যারাগ্রাফের সাথে merge
    private static final int MAX_WORDS = 500;  // এর বেশি হলে ভেঙে দুই ভাগ করা হবে

    public List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return chunks;
        }

        // ১. \n\n দিয়ে প্যারাগ্রাফে ভাগ করো
        String[] rawParagraphs = text.split("\\n\\s*\\n");

        List<String> paragraphs = new ArrayList<>();
        for (String p : rawParagraphs) {
            String cleaned = p.replaceAll("\\s+", " ").trim();
            if (!cleaned.isEmpty()) {
                paragraphs.add(cleaned);
            }
        }

        // ২. ছোট প্যারাগ্রাফ merge করো, বড়টা ভেঙে দাও
        StringBuilder buffer = new StringBuilder();

        for (String para : paragraphs) {
            int paraWordCount = countWords(para);

            if (paraWordCount > MAX_WORDS) {
                // আগে buffer-এ যা জমে ছিল সেটা flush করো
                if (buffer.length() > 0) {
                    chunks.add(buffer.toString().trim());
                    buffer.setLength(0);
                }
                // বড় প্যারাগ্রাফ বাক্য-সীমানা রেখে ভাগ করো
                chunks.addAll(splitLargeParagraph(para));
                continue;
            }

            if (buffer.length() == 0) {
                buffer.append(para);
            } else {
                int combinedWordCount = countWords(buffer.toString()) + paraWordCount;
                if (combinedWordCount <= MAX_WORDS) {
                    buffer.append(" ").append(para);
                } else {
                    chunks.add(buffer.toString().trim());
                    buffer.setLength(0);
                    buffer.append(para);
                }
            }

            // buffer যথেষ্ট বড় হয়ে গেলে flush করো
            if (countWords(buffer.toString()) >= MIN_WORDS && countWords(buffer.toString()) >= MAX_WORDS * 0.6) {
                chunks.add(buffer.toString().trim());
                buffer.setLength(0);
            }
        }

        // শেষে বাকি থাকা buffer flush করো
        if (buffer.length() > 0) {
            String remaining = buffer.toString().trim();
            // খুব ছোট হলে (< MIN_WORDS) আগের chunk-এর সাথে জুড়ে দাও
            if (countWords(remaining) < MIN_WORDS && !chunks.isEmpty()) {
                int lastIndex = chunks.size() - 1;
                chunks.set(lastIndex, chunks.get(lastIndex) + " " + remaining);
            } else {
                chunks.add(remaining);
            }
        }

        return chunks;
    }

    private List<String> splitLargeParagraph(String paragraph) {
        List<String> result = new ArrayList<>();

        // বাংলা/ইংরেজি বাক্য বিভাজক দিয়ে ভাগ করো (। বা .)
        String[] sentences = paragraph.split("(?<=[।.!?])\\s+");

        StringBuilder buffer = new StringBuilder();
        for (String sentence : sentences) {
            int combined = countWords(buffer.toString()) + countWords(sentence);
            if (combined > MAX_WORDS && buffer.length() > 0) {
                result.add(buffer.toString().trim());
                buffer.setLength(0);
            }
            buffer.append(sentence).append(" ");
        }

        if (buffer.length() > 0) {
            result.add(buffer.toString().trim());
        }

        return result;
    }

    private int countWords(String text) {
        if (text == null || text.isBlank()) return 0;
        return text.trim().split("\\s+").length;
    }
}
