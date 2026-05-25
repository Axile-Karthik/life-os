package com.lifeos.metadata.service;

import org.springframework.stereotype.Service;

@Service
public class MetadataNormalizationService {

    /**
     * Normalizes a title for deduplication and matching:
     * - Convert to lowercase
     * - Strip trademark, copyright, and registration symbols (™, ®, ©)
     * - Replace non-alphanumeric characters with spaces
     * - Collapse multiple spaces into a single space
     * - Trim leading and trailing spaces
     */
    public String normalizeTitle(String title) {
        if (title == null) {
            return "";
        }
        return title.toLowerCase()
                .replaceAll("[™®©]", "")
                .replaceAll("[^a-z0-9]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    /**
     * Sanitizes external source names (e.g. "igdb" -> "IGDB").
     */
    public String sanitizeExternalSource(String source) {
        if (source == null) {
            return null;
        }
        return source.trim().toUpperCase();
    }

    /**
     * Sanitizes external IDs by trimming.
     */
    public String sanitizeExternalId(String externalId) {
        if (externalId == null) {
            return null;
        }
        return externalId.trim();
    }
}
