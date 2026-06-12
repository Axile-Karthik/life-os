package com.lifeos.metadata.service;

import com.lifeos.metadata.dto.SearchResultDto;
import com.lifeos.metadata.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ContentSearchService {
    Page<SearchResultDto> searchContent(String query, ContentType type, Pageable pageable);
}
