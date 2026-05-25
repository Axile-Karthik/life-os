package com.lifeos.library.repository;

import com.lifeos.library.entity.LibraryItem;
import com.lifeos.library.enums.LibraryStatus;
import com.lifeos.metadata.enums.ContentType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LibraryItemRepository extends JpaRepository<LibraryItem, UUID> {

    @Query("SELECT li FROM LibraryItem li JOIN FETCH li.metadata m WHERE " +
           "(:status IS NULL OR li.status = :status) AND " +
           "(:contentType IS NULL OR m.contentType = :contentType)")
    Page<LibraryItem> findFiltered(
        @Param("status") LibraryStatus status,
        @Param("contentType") ContentType contentType,
        Pageable pageable
    );

    @Query("SELECT li FROM LibraryItem li WHERE li.metadata.id = :metadataId")
    Optional<LibraryItem> findByMetadataId(@Param("metadataId") UUID metadataId);
}
