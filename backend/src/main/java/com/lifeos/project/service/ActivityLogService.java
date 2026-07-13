package com.lifeos.project.service;

import com.lifeos.project.entity.Project;
import com.lifeos.project.entity.Board;
import com.lifeos.project.entity.Task;
import com.lifeos.project.enums.ActivityAction;
import com.lifeos.project.dto.ActivityLogDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface ActivityLogService {
    void log(Project project, Board board, Task task, ActivityAction actionType, String details);
    Page<ActivityLogDto> getProjectActivity(UUID projectId, Pageable pageable);
}
