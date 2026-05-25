package com.lifeos.metadata.queue.repository;

import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.queue.entity.MetadataEnrichmentQueue;
import com.lifeos.metadata.queue.enums.QueueStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MetadataEnrichmentQueueRepository extends JpaRepository<MetadataEnrichmentQueue, UUID> {

    boolean existsByNormalizedTitleAndContentTypeAndQueueStatus(
            String normalizedTitle, ContentType contentType, QueueStatus queueStatus);

    boolean existsByNormalizedTitleAndQueueStatus(
            String normalizedTitle, QueueStatus queueStatus);

    List<MetadataEnrichmentQueue> findByQueueStatusOrderByCreatedAtAsc(QueueStatus queueStatus);

    @org.springframework.data.jpa.repository.Query(
        value = "SELECT * FROM metadata_enrichment_queue WHERE queue_status = 'PENDING' ORDER BY created_at ASC LIMIT 1 FOR UPDATE SKIP LOCKED",
        nativeQuery = true
    )
    List<MetadataEnrichmentQueue> findNextPendingJobForUpdate();
}
