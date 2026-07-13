package com.lifeos.project.repository;

import com.lifeos.project.entity.Label;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface LabelRepository extends JpaRepository<Label, UUID> {
    List<Label> findByProjectId(UUID projectId);
}
