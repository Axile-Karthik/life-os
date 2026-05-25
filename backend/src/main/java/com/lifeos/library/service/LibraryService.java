package com.lifeos.library.service;

import com.lifeos.library.dto.CreateLibraryItemRequest;
import com.lifeos.library.dto.LibraryItemDto;
import com.lifeos.library.entity.LibraryItem;
import com.lifeos.library.enums.LibraryStatus;
import com.lifeos.library.mapper.LibraryMapper;
import com.lifeos.library.repository.LibraryItemRepository;
import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.enums.MetadataState;
import com.lifeos.metadata.queue.service.MetadataEnrichmentQueueService;
import com.lifeos.metadata.repository.ContentMetadataRepository;
import com.lifeos.metadata.service.MetadataNormalizationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LibraryService {

    private final LibraryItemRepository libraryItemRepository;
    private final ContentMetadataRepository contentMetadataRepository;
    private final MetadataEnrichmentQueueService queueService;
    private final MetadataNormalizationService metadataNormalizationService;
    private final LibraryMapper libraryMapper;

    @Transactional(readOnly = true)
    public Page<LibraryItemDto> getLibrary(LibraryStatus status, ContentType type, String sort, Pageable pageable) {
        // Map sort string to Sort object
        Sort sortObj = Sort.by(Sort.Direction.DESC, "lastActivityAt");
        if ("LAST_ACTIVE".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "lastActivityAt");
        } else if ("UPDATED".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.DESC, "updatedAt");
        } else if ("TITLE".equalsIgnoreCase(sort)) {
            sortObj = Sort.by(Sort.Direction.ASC, "metadata.title");
        }

        Pageable sortedPageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), sortObj);
        
        Page<LibraryItem> items = libraryItemRepository.findFiltered(status, type, sortedPageable);
        return items.map(libraryMapper::toDto);
    }

    @Transactional
    public LibraryItemDto addToLibrary(CreateLibraryItemRequest request) {
        log.info("Adding content to library: externalSource={}, externalId={}, status={}",
                request.getExternalSource(), request.getExternalId(), request.getStatus());

        // 1. Resolve Metadata
        ContentMetadata metadata = null;
        boolean isNewMetadata = false;

        String sanitizedSource = metadataNormalizationService.sanitizeExternalSource(request.getExternalSource());
        String sanitizedId = metadataNormalizationService.sanitizeExternalId(request.getExternalId());

        // Try lookup by external mappings first
        if (sanitizedSource != null && sanitizedId != null) {
            Optional<ContentMetadata> existing = contentMetadataRepository
                    .findByExternalSourceAndExternalId(sanitizedSource, sanitizedId);
            if (existing.isPresent()) {
                metadata = existing.get();
            }
        }

        // Try lookup by title and type next
        if (metadata == null && request.getTitle() != null && request.getContentType() != null) {
            ContentType type = parseContentType(request.getContentType());
            String normTitle = metadataNormalizationService.normalizeTitle(request.getTitle());
            List<ContentMetadata> matches = contentMetadataRepository.findByNormalizedTitleAndContentType(normTitle, type);
            if (!matches.isEmpty()) {
                metadata = matches.getFirst();
            }
        }

        // Bootstrap metadata if still not found
        if (metadata == null) {
            isNewMetadata = true;
            ContentType type = parseContentType(request.getContentType());
            LocalDate releaseDate = null;
            if (request.getReleaseDate() != null && !request.getReleaseDate().isBlank()) {
                try {
                    releaseDate = LocalDate.parse(request.getReleaseDate());
                } catch (Exception e) {
                    try {
                        int year = Integer.parseInt(request.getReleaseDate());
                        releaseDate = LocalDate.of(year, 1, 1);
                    } catch (Exception ignored) {}
                }
            }

            metadata = ContentMetadata.builder()
                    .title(request.getTitle())
                    .normalizedTitle(metadataNormalizationService.normalizeTitle(request.getTitle()))
                    .contentType(type)
                    .imageUrl(request.getImageUrl())
                    .releaseDate(releaseDate)
                    .externalSource(sanitizedSource)
                    .externalId(sanitizedId)
                    .metadataState(MetadataState.PENDING)
                    .build();

            metadata = contentMetadataRepository.save(metadata);
            log.info("Created new metadata stub for: {}", metadata.getTitle());
        }

        // 2. Resolve LibraryItem relationship
        LibraryStatus statusEnum = LibraryStatus.WISHLIST;
        if (request.getStatus() != null) {
            try {
                statusEnum = LibraryStatus.valueOf(request.getStatus().toUpperCase());
            } catch (Exception ignored) {}
        }

        Optional<LibraryItem> existingItem = libraryItemRepository.findByMetadataId(metadata.getId());
        LibraryItem libraryItem;

        if (existingItem.isPresent()) {
            libraryItem = existingItem.get();
            libraryItem.setStatus(statusEnum);
            libraryItem.setUpdatedAt(Instant.now());
        } else {
            libraryItem = LibraryItem.builder()
                    .metadata(metadata)
                    .status(statusEnum)
                    .favorite(false)
                    .progressPercent(0)
                    .sourceType(metadata.getExternalSource())
                    .startedAt(statusEnum == LibraryStatus.PLAYING || statusEnum == LibraryStatus.WATCHING || statusEnum == LibraryStatus.READING ? Instant.now() : null)
                    .build();
        }

        libraryItem = libraryItemRepository.save(libraryItem);

        // 3. Enqueue enrichment if new metadata was created
        if (isNewMetadata && metadata.getExternalSource() != null && metadata.getExternalId() != null) {
            queueService.enqueueJob(metadata.getTitle(), metadata.getContentType(), null, metadata.getExternalSource());
            log.info("Enqueued background metadata enrichment job for metadataId={}", metadata.getId());
        }

        return libraryMapper.toDto(libraryItem);
    }

    @Transactional
    public ContentMetadata resolveMetadataForSession(String title, String typeStr, String source) {
        ContentType type = parseContentType(typeStr);
        String normTitle = metadataNormalizationService.normalizeTitle(title);
        
        List<ContentMetadata> matches = contentMetadataRepository.findByNormalizedTitleAndContentType(normTitle, type);
        if (!matches.isEmpty()) {
            return matches.getFirst();
        }

        // If not found, bootstrap content metadata record
        ContentMetadata metadata = ContentMetadata.builder()
                .title(title)
                .normalizedTitle(normTitle)
                .contentType(type)
                .externalSource(metadataNormalizationService.sanitizeExternalSource(source))
                .metadataState(MetadataState.PENDING)
                .build();

        return contentMetadataRepository.save(metadata);
    }

    @Transactional
    public void updateLibraryStatusForSession(ContentMetadata metadata, String typeStr, Instant activityTime) {
        ContentType type = parseContentType(typeStr);
        LibraryStatus activeStatus = getActiveStatusFromType(type);

        Optional<LibraryItem> existing = libraryItemRepository.findByMetadataId(metadata.getId());
        if (existing.isPresent()) {
            LibraryItem item = existing.get();
            item.setLastActivityAt(activityTime);
            // Upgrade wishlist items or paused items to active playing/watching status
            if (item.getStatus() == LibraryStatus.WISHLIST) {
                item.setStatus(activeStatus);
                if (item.getStartedAt() == null) {
                    item.setStartedAt(activityTime);
                }
            }
            libraryItemRepository.save(item);
        } else {
            LibraryItem item = LibraryItem.builder()
                    .metadata(metadata)
                    .status(activeStatus)
                    .favorite(false)
                    .progressPercent(0)
                    .startedAt(activityTime)
                    .lastActivityAt(activityTime)
                    .build();
            libraryItemRepository.save(item);
        }
    }

    private ContentType parseContentType(String typeStr) {
        if (typeStr == null) return ContentType.GAME;
        try {
            return ContentType.valueOf(typeStr.toUpperCase());
        } catch (Exception e) {
            String clean = typeStr.toLowerCase();
            if (clean.contains("game")) return ContentType.GAME;
            if (clean.contains("movie")) return ContentType.MOVIE;
            if (clean.contains("series") || clean.contains("tv")) return ContentType.SERIES;
            if (clean.contains("anime")) return ContentType.ANIME;
            if (clean.contains("book")) return ContentType.BOOK;
            if (clean.contains("manga")) return ContentType.MANGA;
            if (clean.contains("music")) return ContentType.MUSIC;
            return ContentType.GAME;
        }
    }

    private LibraryStatus getActiveStatusFromType(ContentType type) {
        if (type == null) return LibraryStatus.PLAYING;
        switch (type) {
            case MOVIE:
            case SERIES:
            case ANIME:
                return LibraryStatus.WATCHING;
            case BOOK:
            case MANGA:
                return LibraryStatus.READING;
            case MUSIC:
            case GAME:
            default:
                return LibraryStatus.PLAYING;
        }
    }
}
