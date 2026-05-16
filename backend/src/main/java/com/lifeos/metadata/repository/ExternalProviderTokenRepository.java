package com.lifeos.metadata.repository;

import com.lifeos.metadata.entity.ExternalProviderTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExternalProviderTokenRepository extends JpaRepository<ExternalProviderTokenEntity, Long> {
    Optional<ExternalProviderTokenEntity> findByProvider(String provider);
}
