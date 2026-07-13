package com.lifeos.project.repository;

import com.lifeos.project.entity.Board;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface BoardRepository extends JpaRepository<Board, UUID> {
    List<Board> findByProjectId(UUID projectId);
}
