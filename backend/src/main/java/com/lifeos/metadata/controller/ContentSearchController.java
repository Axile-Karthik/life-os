package com.lifeos.metadata.controller;

import com.lifeos.metadata.dto.SearchResultDto;
import com.lifeos.metadata.enums.ContentType;
import com.lifeos.metadata.service.ContentSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/metadata/search")
@RequiredArgsConstructor
public class ContentSearchController {

    private final ContentSearchService searchService;

    @GetMapping
    public ResponseEntity<Page<SearchResultDto>> searchContent(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        ContentType contentType = null;
        if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            try {
                contentType = ContentType.valueOf(type.toUpperCase());
            } catch (Exception ignored) {}
        }
        
        Page<SearchResultDto> results = searchService.searchContent(query, contentType, pageable);
        
        return ResponseEntity.ok(results);
    }
}
