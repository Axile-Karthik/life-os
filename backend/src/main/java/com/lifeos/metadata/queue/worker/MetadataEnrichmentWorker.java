package com.lifeos.metadata.queue.worker;

import com.lifeos.metadata.dto.SearchResultDto;
import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.MetadataState;
import com.lifeos.metadata.provider.MetadataProvider;
import com.lifeos.metadata.queue.entity.MetadataEnrichmentQueue;
import com.lifeos.metadata.queue.enums.QueueStatus;
import com.lifeos.metadata.queue.repository.MetadataEnrichmentQueueRepository;
import com.lifeos.metadata.queue.service.MetadataEnrichmentQueueService;
import com.lifeos.metadata.repository.ContentMetadataRepository;
import com.lifeos.session.worker.SessionLinkWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MetadataEnrichmentWorker {

    private final MetadataEnrichmentQueueRepository queueRepository;
    private final MetadataEnrichmentQueueService queueService;
    private final ContentMetadataRepository contentMetadataRepository;
    private final List<MetadataProvider> providers;
    private final SessionLinkWorker sessionLinkWorker;

    /**
     * Called by the PostgresNotifyListener when a new job arrives,
     * or periodically as a fallback.
     */
    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedDelayString = "${lifeos.worker.metadata-enrichment.delay:60000}")
    public void processPendingJobs() {
        boolean hasMoreJobs = true;
        int processedCount = 0;

        while (hasMoreJobs) {
            Optional<MetadataEnrichmentQueue> jobOpt = queueService.claimNextPendingJob();
            
            if (jobOpt.isEmpty()) {
                hasMoreJobs = false;
                if (processedCount > 0) {
                    log.info("MetadataEnrichmentWorker completed processing {} jobs.", processedCount);
                }
                break;
            }

            MetadataEnrichmentQueue job = jobOpt.get();
            processedCount++;

            try {
                // 1. See if metadata already magically exists (maybe created by UI search concurrently)
                boolean alreadyExists = false;
                if (job.getContentType() != null) {
                    alreadyExists = !contentMetadataRepository.findByNormalizedTitleAndContentType(
                            job.getNormalizedTitle(), job.getContentType()).isEmpty();
                }

                if (alreadyExists) {
                    log.info("Metadata for '{}' already exists. Skipping enrichment.", job.getNormalizedTitle());
                    queueService.markCompleted(job);
                    continue;
                }

                // 2. Search Providers
                SearchResultDto bestMatch = null;
                for (MetadataProvider provider : providers) {
                    if (provider.supports(job.getContentType())) {
                        List<SearchResultDto> results = provider.search(job.getRawTitle());
                        if (results != null && !results.isEmpty()) {
                            bestMatch = results.getFirst(); // Take top result
                            break;
                        }
                    }
                }

                // 3. Create Canonical Metadata
                if (bestMatch != null) {
                    ContentMetadata metadata = ContentMetadata.builder()
                            .title(bestMatch.getTitle() != null ? bestMatch.getTitle() : job.getRawTitle())
                            .normalizedTitle(job.getNormalizedTitle())
                            .contentType(job.getContentType())
                            .externalSource(bestMatch.getExternalSource())
                            .externalId(bestMatch.getExternalId())
                            .imageUrl(bestMatch.getImageUrl())
                            .releaseDate(bestMatch.getReleaseDate())
                            .metadataState(MetadataState.READY) // Mark as resolved
                            .build();

                    contentMetadataRepository.save(metadata);
                    log.info("Successfully enriched metadata for '{}' via {}", job.getNormalizedTitle(), bestMatch.getExternalSource());
                    
                    queueService.markCompleted(job);
                    
                    // 4. Trigger session linker to immediately link unlinked telemetry
                    sessionLinkWorker.linkPendingSessions();
                } else {
                    queueService.markFailed(job, "No matches found from any provider.");
                }

            } catch (Exception e) {
                log.error("Failed to process enrichment job for '{}'", job.getNormalizedTitle(), e);
                queueService.markFailed(job, e.getMessage());
            }
        }
    }
}
