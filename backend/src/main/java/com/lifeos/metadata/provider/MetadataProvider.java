package com.lifeos.metadata.provider;

import com.lifeos.metadata.dto.SearchResultDto;
import com.lifeos.metadata.enums.ContentType;

import java.util.List;

public interface MetadataProvider {
    /**
     * Checks if this provider supports searching for the given content type.
     */
    boolean supports(ContentType contentType);

    /**
     * Searches the provider for the given query.
     */
    List<SearchResultDto> search(String query);
}
