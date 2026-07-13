package com.lifeos.project.service.impl;

import com.lifeos.shared.kernel.exception.ApiException;
import com.lifeos.project.dto.*;
import com.lifeos.project.entity.*;
import com.lifeos.project.enums.ActivityAction;
import com.lifeos.project.mapper.ProjectMapper;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.project.repository.BoardRepository;
import com.lifeos.project.repository.BoardColumnRepository;
import com.lifeos.project.repository.TaskRepository;
import com.lifeos.project.service.ProjectService;
import com.lifeos.project.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectServiceImpl implements ProjectService {

    private final ProjectRepository projectRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnRepository columnRepository;
    private final TaskRepository taskRepository;
    private final ProjectMapper projectMapper;
    private final ActivityLogService logService;

    @Override
    @Transactional
    public ProjectDto createProject(CreateProjectRequest request) {
        String prefix = request.ticketPrefix();
        if (prefix == null || prefix.trim().isEmpty()) {
            String nameAlphanumeric = request.name().replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
            if (nameAlphanumeric.length() >= 3) {
                prefix = nameAlphanumeric.substring(0, 3);
            } else if (nameAlphanumeric.isEmpty()) {
                prefix = "PRJ";
            } else {
                prefix = nameAlphanumeric + "XXX".substring(0, 3 - nameAlphanumeric.length());
            }
        } else {
            prefix = prefix.trim().toUpperCase();
        }

        Project project = Project.builder()
                .name(request.name())
                .description(request.description())
                .ticketPrefix(prefix)
                .nextTicketNumber(1L)
                .build();

        Project savedProject = projectRepository.save(project);

        Board defaultBoard = Board.builder()
                .name("Default Board")
                .description("Default board for project " + request.name())
                .project(savedProject)
                .build();
        Board savedBoard = boardRepository.save(defaultBoard);

        String[] defaultCols = {"Backlog", "Todo", "In Progress", "Review", "Done"};
        for (int i = 0; i < defaultCols.length; i++) {
            BoardColumn column = BoardColumn.builder()
                    .name(defaultCols[i])
                    .position(i)
                    .board(savedBoard)
                    .build();
            columnRepository.save(column);
        }

        logService.log(savedProject, null, null, ActivityAction.CREATE_PROJECT, 
                "Project '" + savedProject.getName() + "' created with ticket prefix '" + prefix + "'.");

        return projectMapper.toDto(savedProject);
    }

    @Override
    @Transactional(readOnly = true)
    public ProjectDto getProjectById(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException("Project not found", HttpStatus.NOT_FOUND));
        return projectMapper.toDto(project);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProjectDto> getProjects(String search, Pageable pageable) {
        Page<Project> projects;
        if (search != null && !search.trim().isEmpty()) {
            projects = projectRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            projects = projectRepository.findAll(pageable);
        }
        return projects.map(projectMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProjectDto> getAllProjects() {
        return projectRepository.findAll().stream()
                .map(projectMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public ProjectDto updateProject(UUID id, UpdateProjectRequest request) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException("Project not found", HttpStatus.NOT_FOUND));

        project.setName(request.name());
        project.setDescription(request.description());
        Project saved = projectRepository.save(project);

        logService.log(saved, null, null, ActivityAction.UPDATE_PROJECT, 
                "Project details updated.");

        return projectMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteProject(UUID id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ApiException("Project not found", HttpStatus.NOT_FOUND));

        Instant now = Instant.now();
        project.setDeletedAt(now);
        for (Board board : project.getBoards()) {
            board.setDeletedAt(now);
            for (BoardColumn col : board.getColumns()) {
                List<Task> tasks = taskRepository.findByColumnId(col.getId());
                for (Task task : tasks) {
                    task.setDeletedAt(now);
                    taskRepository.save(task);
                }
            }
            boardRepository.save(board);
        }
        projectRepository.save(project);

        logService.log(project, null, null, ActivityAction.DELETE_PROJECT, 
                "Project '" + project.getName() + "' soft-deleted.");
    }
}
