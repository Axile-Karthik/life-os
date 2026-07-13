package com.lifeos.project.service;

import com.lifeos.project.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.UUID;

public interface ProjectService {
    ProjectDto createProject(CreateProjectRequest request);
    ProjectDto getProjectById(UUID id);
    Page<ProjectDto> getProjects(String search, Pageable pageable);
    List<ProjectDto> getAllProjects();
    ProjectDto updateProject(UUID id, UpdateProjectRequest request);
    void deleteProject(UUID id);
}
