package com.lifeos.project.repository;

import com.lifeos.project.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface TaskRepository extends JpaRepository<Task, UUID> {
    List<Task> findByColumnId(UUID columnId);

    List<Task> findByColumnIdAndParentIsNullOrderByPositionAsc(UUID columnId);
    
    List<Task> findByColumnIdAndParentIdOrderByPositionAsc(UUID columnId, UUID parentId);

    @Query("SELECT t FROM Task t WHERE t.column.board.id = :boardId AND t.parent IS NULL")
    List<Task> findByBoardId(UUID boardId);

    @Query("SELECT t FROM Task t WHERE t.column.board.id = :boardId AND t.parent IS NULL " +
           "AND (:priority IS NULL OR t.priority = :priority) " +
           "AND (:search IS NULL OR LOWER(t.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :search, '%')))")
    Page<Task> findFilteredAndPaged(@Param("boardId") UUID boardId, @Param("priority") String priority, @Param("search") String search, Pageable pageable);

    /**
     * PostgreSQL Full-Text Search using tsvector/tsquery with weighted ranking.
     * Searches across title (weight A), description (weight B), and ticket_key (weight C).
     * Results are ordered by relevance rank (ts_rank_cd) descending.
     */
    @Query(value = "SELECT t.* FROM task t " +
           "JOIN board_column bc ON t.column_id = bc.id " +
           "WHERE bc.board_id = :boardId " +
           "AND t.parent_id IS NULL " +
           "AND t.deleted_at IS NULL " +
           "AND (:priority IS NULL OR t.priority = CAST(:priority AS VARCHAR)) " +
           "AND t.search_vector @@ plainto_tsquery('english', :search) " +
           "ORDER BY ts_rank_cd(t.search_vector, plainto_tsquery('english', :search)) DESC",
           countQuery = "SELECT count(*) FROM task t " +
           "JOIN board_column bc ON t.column_id = bc.id " +
           "WHERE bc.board_id = :boardId " +
           "AND t.parent_id IS NULL " +
           "AND t.deleted_at IS NULL " +
           "AND (:priority IS NULL OR t.priority = CAST(:priority AS VARCHAR)) " +
           "AND t.search_vector @@ plainto_tsquery('english', :search)",
           nativeQuery = true)
    Page<Task> fullTextSearch(@Param("boardId") UUID boardId, @Param("priority") String priority, @Param("search") String search, Pageable pageable);
}
