package com.lifeos.library.dto;

import com.lifeos.library.enums.LibraryStatus;
import com.lifeos.metadata.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LibraryItemDto {
    private UUID id;
    private UUID metadataId;
    private String title;
    private String imageUrl;
    private ContentType contentType;
    private LibraryStatus status;
    private Integer progressPercent;
    private Double rating;
    private Boolean favorite;
    private Instant startedAt;
    private Instant completedAt;
    private Instant lastActivityAt;
}
