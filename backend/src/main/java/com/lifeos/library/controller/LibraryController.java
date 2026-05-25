package com.lifeos.library.controller;

import com.lifeos.library.dto.CreateLibraryItemRequest;
import com.lifeos.library.dto.LibraryItemDto;
import com.lifeos.library.enums.LibraryStatus;
import com.lifeos.metadata.enums.ContentType;
import com.lifeos.library.service.LibraryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    @GetMapping
    public ResponseEntity<Page<LibraryItemDto>> getLibrary(
            @RequestParam(required = false) LibraryStatus status,
            @RequestParam(required = false) String type,
            @RequestParam(required = false, defaultValue = "LAST_ACTIVE") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        
        ContentType contentType = null;
        if (type != null && !type.trim().isEmpty() && !"ALL".equalsIgnoreCase(type)) {
            try {
                contentType = ContentType.valueOf(type.toUpperCase());
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok(libraryService.getLibrary(status, contentType, sort, pageable));
    }

    @PostMapping("/items")
    public ResponseEntity<LibraryItemDto> addToLibrary(@RequestBody CreateLibraryItemRequest request) {
        return ResponseEntity.ok(libraryService.addToLibrary(request));
    }
}
