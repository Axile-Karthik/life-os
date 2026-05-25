package com.lifeos.metadata.repository;

import com.lifeos.metadata.entity.ContentMetadata;
import com.lifeos.metadata.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ContentMetadataRepository extends JpaRepository<ContentMetadata, UUID> {

    @Query("SELECT c FROM ContentMetadata c WHERE LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.normalizedTitle) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<ContentMetadata> searchByQuery(@Param("query") String query, Pageable pageable);

    @Query("SELECT c FROM ContentMetadata c WHERE (LOWER(c.title) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(c.normalizedTitle) LIKE LOWER(CONCAT('%', :query, '%'))) AND c.contentType = :type")
    Page<ContentMetadata> searchByQueryAndType(@Param("query") String query, @Param("type") ContentType type, Pageable pageable);

    @Query("SELECT c FROM ContentMetadata c WHERE c.externalSource = :source AND c.externalId = :id")
    Optional<ContentMetadata> findByExternalSourceAndExternalId(@Param("source") String source, @Param("id") String id);

    @Query("SELECT c FROM ContentMetadata c WHERE LOWER(c.normalizedTitle) = LOWER(:normalizedTitle) AND c.contentType = :type")
    List<ContentMetadata> findByNormalizedTitleAndContentType(@Param("normalizedTitle") String normalizedTitle, @Param("type") ContentType type);

    @Query("SELECT c FROM ContentMetadata c WHERE LOWER(c.normalizedTitle) = LOWER(:normalizedTitle)")
    List<ContentMetadata> findByNormalizedTitle(@Param("normalizedTitle") String normalizedTitle);
}
