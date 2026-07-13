package com.lifeos.project.service.impl;

import com.lifeos.project.dto.ActivityLogDto;
import com.lifeos.project.entity.*;
import com.lifeos.project.enums.ActivityAction;
import com.lifeos.project.mapper.ActivityLogMapper;
import com.lifeos.project.repository.ActivityLogRepository;
import com.lifeos.project.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ActivityLogServiceImpl implements ActivityLogService {

    private final ActivityLogRepository logRepository;
    private final ActivityLogMapper logMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRED)
    public void log(Project project, Board board, Task task, ActivityAction actionType, String details) {
        ActivityLog logEntity = ActivityLog.builder()
                .project(project)
                .board(board)
                .task(task)
                .actionType(actionType)
                .details(details)
                .build();

        logRepository.save(logEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ActivityLogDto> getProjectActivity(UUID projectId, Pageable pageable) {
        return logRepository.findByProjectIdOrderByCreatedAtDesc(projectId, pageable)
                .map(logMapper::toDto);
    }
}
