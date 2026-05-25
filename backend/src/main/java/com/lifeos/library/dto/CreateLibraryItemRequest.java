package com.lifeos.library.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLibraryItemRequest {
    private String externalSource;
    private String externalId;
    private String status; // LibraryStatus

    // Optional metadata bootstrapping fields to avoid blocking on external calls
    private String title;
    private String imageUrl;
    private String contentType; // ContentType string
    private String releaseDate; // ISO format or simple year
}
