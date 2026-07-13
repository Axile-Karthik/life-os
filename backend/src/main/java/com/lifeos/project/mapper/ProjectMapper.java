package com.lifeos.project.mapper;

import com.lifeos.project.dto.ProjectDto;
import com.lifeos.project.entity.Project;
import org.springframework.stereotype.Component;

@Component
public class ProjectMapper {

    public ProjectDto toDto(Project entity) {
        if (entity == null) return null;
        return ProjectDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .description(entity.getDescription())
                .ticketPrefix(entity.getTicketPrefix())
                .version(entity.getVersion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
