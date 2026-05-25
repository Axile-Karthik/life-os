package com.lifeos.session.worker;

import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.repository.ContentMetadataRepository;
import com.lifeos.session.entity.ActivitySession;
import com.lifeos.session.repository.ActivitySessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class SessionLinkWorker {

    private final ActivitySessionRepository sessionRepository;
    private final ContentMetadataRepository contentMetadataRepository;

    /**
     * Periodically sweeps unlinked sessions and attempts to link them to metadata
     * if the metadata enrichment queue has resolved them.
     */
    @Scheduled(fixedDelayString = "${lifeos.worker.session-link.delay:60000}")
    @Transactional
    public void linkPendingSessions() {
        List<ActivitySession> unlinkedSessions = sessionRepository.findByMetadataIsNull();

        if (unlinkedSessions.isEmpty()) {
            return;
        }

        int linkedCount = 0;

        for (ActivitySession session : unlinkedSessions) {
            if (session.getNormalizedTitle() == null || session.getNormalizedTitle().isEmpty()) {
                continue;
            }

            // Attempt to find metadata
            List<ContentMetadata> matches;
            if (session.getContentType() != null) {
                matches = contentMetadataRepository.findByNormalizedTitleAndContentType(
                        session.getNormalizedTitle(), session.getContentType());
            } else {
                matches = contentMetadataRepository.findByNormalizedTitle(session.getNormalizedTitle());
            }

            if (!matches.isEmpty()) {
                session.setMetadata(matches.get(0));
                sessionRepository.save(session);
                linkedCount++;
                log.info("Asynchronously linked session {} to metadata: {}", session.getId(), matches.get(0).getTitle());
            }
        }

        if (linkedCount > 0) {
            log.info("Successfully linked {} previously unlinked activity sessions to metadata.", linkedCount);
        }
    }
}
