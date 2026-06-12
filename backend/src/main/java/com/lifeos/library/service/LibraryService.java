package com.lifeos.library.service;

import com.lifeos.library.dto.CreateLibraryItemRequest;
import com.lifeos.library.dto.LibraryItemDto;
import com.lifeos.library.enums.LibraryStatus;
import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface LibraryService {
    Page<LibraryItemDto> getLibrary(LibraryStatus status, ContentType type, String sort, Pageable pageable);
    LibraryItemDto addToLibrary(CreateLibraryItemRequest request);
    ContentMetadata resolveMetadataForSession(String title, String typeStr, String source);
    void updateLibraryStatusForSession(ContentMetadata metadata, String typeStr, Instant activityTime);
}
