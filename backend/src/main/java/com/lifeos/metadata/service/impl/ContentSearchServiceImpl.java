package com.lifeos.metadata.service.impl;

import com.lifeos.metadata.dto.SearchResultDto;
import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.provider.MetadataProvider;
import com.lifeos.metadata.repository.ContentMetadataRepository;
import com.lifeos.metadata.service.ContentSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContentSearchServiceImpl implements ContentSearchService {

    private final ContentMetadataRepository repository;
    private final List<MetadataProvider> providers;

    @Override
    public Page<SearchResultDto> searchContent(String query, ContentType type, Pageable pageable) {
        if (query == null || query.trim().isEmpty()) {
            query = "";
        }
        
        Page<ContentMetadata> localResults;
        if (type == null) {
            localResults = repository.searchByQuery(query, pageable);
        } else {
            log.info("from DB");
            localResults = repository.searchByQueryAndType(query, type, pageable);
        }

        // Return local results immediately if we found any meaningful tracked content
        if (localResults.hasContent()) {
            List<SearchResultDto> dtos = localResults.getContent().stream()
                    .map(this::toDto)
                    .collect(Collectors.toList());
            return new PageImpl<>(dtos, pageable, localResults.getTotalElements());
        }

        // Local search returned 0 results. Fallback to external providers for discovery.
        // We only do this on the first page to keep it simple.
        if (pageable.getPageNumber() == 0 && !query.isBlank()) {
            log.info("from external ------->");
            for (MetadataProvider provider : providers) {
                if (provider.supports(type)) {
                    List<SearchResultDto> externalResults = provider.search(query);
                    if (!externalResults.isEmpty()) {
                        return new PageImpl<>(externalResults, pageable, externalResults.size());
                    }
                }
            }
        }

        return new PageImpl<>(List.of(), pageable, 0);
    }

    private SearchResultDto toDto(ContentMetadata entity) {
        return SearchResultDto.builder()
                .id(String.valueOf(entity.getId()))
                .title(entity.getTitle())
                .imageUrl(entity.getImageUrl())
                .releaseDate(entity.getReleaseDate())
                .contentType(entity.getContentType())
                .externalSource(entity.getExternalSource())
                .externalId(entity.getExternalId())
                .build();
    }
}
