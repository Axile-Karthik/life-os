package com.lifeos.project.repository;

import com.lifeos.project.entity.ActivityLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ActivityLogRepository extends JpaRepository<ActivityLog, UUID> {
    Page<ActivityLog> findByProjectIdOrderByCreatedAtDesc(UUID projectId, Pageable pageable);
}
