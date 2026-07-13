package com.lifeos.project.service.impl;

import com.lifeos.shared.kernel.exception.ApiException;
import com.lifeos.project.dto.CreateLabelRequest;
import com.lifeos.project.dto.LabelDto;
import com.lifeos.project.entity.Label;
import com.lifeos.project.entity.Project;
import com.lifeos.project.mapper.LabelMapper;
import com.lifeos.project.repository.LabelRepository;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.project.service.LabelService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LabelServiceImpl implements LabelService {

    private final LabelRepository labelRepository;
    private final ProjectRepository projectRepository;
    private final LabelMapper labelMapper;

    @Override
    @Transactional
    public LabelDto createLabel(UUID projectId, CreateLabelRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found", HttpStatus.NOT_FOUND));

        Label label = Label.builder()
                .name(request.name())
                .color(request.color())
                .project(project)
                .build();

        Label saved = labelRepository.save(label);
        return labelMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<LabelDto> getLabelsByProject(UUID projectId) {
        return labelRepository.findByProjectId(projectId).stream()
                .map(labelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteLabel(UUID id) {
        Label label = labelRepository.findById(id)
                .orElseThrow(() -> new ApiException("Label not found", HttpStatus.NOT_FOUND));
        labelRepository.delete(label);
    }
}
