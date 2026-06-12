package com.lifeos.metadata.service;

public interface MetadataNormalizationService {
    String normalizeTitle(String title);
    String sanitizeExternalSource(String source);
    String sanitizeExternalId(String externalId);
}
