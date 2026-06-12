package com.lifeos.metadata.service.impl;

import com.lifeos.metadata.service.MetadataNormalizationService;
import org.springframework.stereotype.Service;

@Service
public class MetadataNormalizationServiceImpl implements MetadataNormalizationService {

    @Override
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

    @Override
    public String sanitizeExternalSource(String source) {
        if (source == null) {
            return null;
        }
        return source.trim().toUpperCase();
    }

    @Override
    public String sanitizeExternalId(String externalId) {
        if (externalId == null) {
            return null;
        }
        return externalId.trim();
    }
}
