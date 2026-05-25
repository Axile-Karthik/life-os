package com.lifeos.metadata.queue.service;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.queue.entity.MetadataEnrichmentQueue;
import com.lifeos.metadata.queue.enums.QueueStatus;
import com.lifeos.metadata.queue.repository.MetadataEnrichmentQueueRepository;
import com.lifeos.metadata.service.MetadataNormalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataEnrichmentQueueService {

    private final MetadataEnrichmentQueueRepository queueRepository;
    private final MetadataNormalizationService normalizationService;

    @Transactional
    public void enqueueJob(String rawTitle, ContentType contentType, String sourcePlatform, String externalSource) {
        String normalizedTitle = normalizationService.normalizeTitle(rawTitle);

        if (normalizedTitle == null || normalizedTitle.isEmpty()) {
            log.warn("Cannot enqueue enrichment job with empty normalized title (raw: '{}')", rawTitle);
            return;
        }

        // Deduplication rule: If a PENDING job already exists for this title & type, do not enqueue again
        boolean exists;
        if (contentType != null) {
            exists = queueRepository.existsByNormalizedTitleAndContentTypeAndQueueStatus(
                    normalizedTitle, contentType, QueueStatus.PENDING);
        } else {
            exists = queueRepository.existsByNormalizedTitleAndQueueStatus(
                    normalizedTitle, QueueStatus.PENDING);
        }

        if (exists) {
            log.debug("Enrichment job for '{}' ({}) is already PENDING. Skipping duplicate.", normalizedTitle, contentType);
            return;
        }

        MetadataEnrichmentQueue queueItem = MetadataEnrichmentQueue.builder()
                .rawTitle(rawTitle)
                .normalizedTitle(normalizedTitle)
                .contentType(contentType)
                .sourcePlatform(sourcePlatform)
                .externalSource(externalSource)
                .queueStatus(QueueStatus.PENDING)
                .build();

        queueRepository.save(queueItem);
        log.info("Enqueued metadata enrichment job for '{}'", normalizedTitle);
    }

    @Transactional
    public java.util.Optional<MetadataEnrichmentQueue> claimNextPendingJob() {
        java.util.List<MetadataEnrichmentQueue> jobs = queueRepository.findNextPendingJobForUpdate();
        if (jobs.isEmpty()) {
            return java.util.Optional.empty();
        }
        MetadataEnrichmentQueue job = jobs.get(0);
        job.setQueueStatus(QueueStatus.PROCESSING);
        return java.util.Optional.of(queueRepository.save(job));
    }

    @Transactional
    public void markProcessing(MetadataEnrichmentQueue job) {
        job.setQueueStatus(QueueStatus.PROCESSING);
        queueRepository.save(job);
    }

    @Transactional
    public void markCompleted(MetadataEnrichmentQueue job) {
        job.setQueueStatus(QueueStatus.COMPLETED);
        job.setProcessedAt(Instant.now());
        queueRepository.save(job);
    }

    @Transactional
    public void markFailed(MetadataEnrichmentQueue job, String errorReason) {
        job.setLastError(errorReason);
        job.setRetryCount(job.getRetryCount() + 1);

        if (job.getRetryCount() >= 5) {
            job.setQueueStatus(QueueStatus.FAILED);
            log.error("Enrichment job for '{}' FAILED after {} retries. Reason: {}", 
                      job.getNormalizedTitle(), job.getRetryCount(), errorReason);
        } else {
            job.setQueueStatus(QueueStatus.PENDING); // Re-queue for next attempt
            log.warn("Enrichment job for '{}' failed (retry {}). Reason: {}", 
                     job.getNormalizedTitle(), job.getRetryCount(), errorReason);
        }

        queueRepository.save(job);
    }
}
