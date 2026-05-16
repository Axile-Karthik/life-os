package com.lifeos.metadata.repository;

import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ContentMetadataRepository extends JpaRepository<ContentMetadata, Long> {

    @Query("SELECT c FROM ContentMetadata c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.normalizedTitle) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ContentMetadata> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM ContentMetadata c WHERE (LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.normalizedTitle) LIKE LOWER(CONCAT('%', :query, '%'))) AND c.contentType = :type")
    Page<ContentMetadata> searchByQueryAndType(@Param("query") String query, @Param("type") ContentType type, Pageable pageable);
}
