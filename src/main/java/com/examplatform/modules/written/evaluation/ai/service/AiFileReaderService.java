package com.examplatform.modules.written.evaluation.ai.service;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
public class AiFileReaderService {

    /**
     * Reads a file from local storage path and returns it as a Base64-encoded string.
     * Currently assumes fileUrl is a local filesystem path
     * (e.g. /tmp/written-submissions/xxx.pdf or /tmp/written-submissions/xxx.jpg).
     * When real object storage (S3/Cloud Storage/etc.) is added later,
     * this method should be updated to download from the remote URL instead.
     */
    public String readAsBase64(String fileUrl) {
        try {
            Path path = resolvePath(fileUrl);
            byte[] bytes = Files.readAllBytes(path);
            return Base64.getEncoder().encodeToString(bytes);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read file for AI processing: " + fileUrl, e);
        }
    }

    /**
     * Detects the MIME type of a file based on its extension.
     * Falls back to Files.probeContentType, then to a generic default.
     */
    public String detectMimeType(String fileUrl) {
        try {
            Path path = resolvePath(fileUrl);
            String probed = Files.probeContentType(path);
            if (probed != null) {
                return probed;
            }
        } catch (IOException ignored) {
            // fall through to extension-based detection
        }

        String lower = fileUrl.toLowerCase();
        if (lower.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lower.endsWith(".png")) {
            return "image/png";
        } else if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lower.endsWith(".webp")) {
            return "image/webp";
        }
        return "application/octet-stream";
    }

    private Path resolvePath(String fileUrl) {
        // fileUrl is currently a raw local path; if it later becomes a URL
        // (http://... or storage://...), this needs to be updated to fetch remotely instead.
        if (fileUrl == null || fileUrl.isBlank()) {
            throw new IllegalArgumentException("fileUrl is null or blank, cannot read file for AI processing");
        }
        return Paths.get(fileUrl);
    }
}
