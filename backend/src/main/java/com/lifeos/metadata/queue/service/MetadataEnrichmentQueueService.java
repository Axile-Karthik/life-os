package com.lifeos.metadata.queue.service;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.queue.entity.MetadataEnrichmentQueue;
import java.util.Optional;

public interface MetadataEnrichmentQueueService {
    void enqueueJob(String rawTitle, ContentType contentType, String sourcePlatform, String externalSource);
    Optional<MetadataEnrichmentQueue> claimNextPendingJob();
    void markProcessing(MetadataEnrichmentQueue job);
    void markCompleted(MetadataEnrichmentQueue job);
    void markFailed(MetadataEnrichmentQueue job, String errorReason);
}
