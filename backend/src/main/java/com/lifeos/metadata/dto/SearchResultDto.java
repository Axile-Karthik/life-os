package com.lifeos.metadata.dto;

import com.lifeos.metadata.enums.ContentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResultDto {
    private String id;
    private String title;
    private String imageUrl;
    private LocalDate releaseDate;
    private ContentType contentType;
    private String externalSource;
    private String externalId;
}
