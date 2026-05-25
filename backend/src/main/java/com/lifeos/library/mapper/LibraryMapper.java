package com.lifeos.library.mapper;

import com.lifeos.library.dto.LibraryItemDto;
import com.lifeos.library.entity.LibraryItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class LibraryMapper {

    public LibraryItemDto toDto(LibraryItem entity) {
        if (entity == null) {
            return null;
        }

        Double doubleRating = null;
        if (entity.getRating() != null) {
            doubleRating = entity.getRating().doubleValue();
        }

        return LibraryItemDto.builder()
                .id(entity.getId())
                .metadataId(entity.getMetadata().getId())
                .title(entity.getMetadata().getTitle())
                .imageUrl(entity.getMetadata().getImageUrl())
                .contentType(entity.getMetadata().getContentType())
                .status(entity.getStatus())
                .progressPercent(entity.getProgressPercent())
                .rating(doubleRating)
                .favorite(entity.getFavorite())
                .startedAt(entity.getStartedAt())
                .completedAt(entity.getCompletedAt())
                .lastActivityAt(entity.getLastActivityAt())
                .build();
    }
}
