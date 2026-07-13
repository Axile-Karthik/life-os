package com.lifeos.project.mapper;

import com.lifeos.project.dto.ActivityLogDto;
import com.lifeos.project.entity.ActivityLog;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLogDto toDto(ActivityLog entity) {
        if (entity == null) return null;
        return ActivityLogDto.builder()
                .id(entity.getId())
                .projectId(entity.getProject() != null ? entity.getProject().getId() : null)
                .boardId(entity.getBoard() != null ? entity.getBoard().getId() : null)
                .taskId(entity.getTask() != null ? entity.getTask().getId() : null)
                .taskTitle(entity.getTask() != null ? entity.getTask().getTitle() : null)
                .actionType(entity.getActionType())
                .details(entity.getDetails())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
