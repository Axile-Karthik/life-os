package com.lifeos.project.service;

import com.lifeos.project.dto.CreateLabelRequest;
import com.lifeos.project.dto.LabelDto;
import java.util.List;
import java.util.UUID;

public interface LabelService {
    LabelDto createLabel(UUID projectId, CreateLabelRequest request);
    List<LabelDto> getLabelsByProject(UUID projectId);
    void deleteLabel(UUID id);
}
