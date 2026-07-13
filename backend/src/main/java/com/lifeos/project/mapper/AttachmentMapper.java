package com.lifeos.project.mapper;

import com.lifeos.project.dto.AttachmentDto;
import com.lifeos.project.entity.Attachment;
import org.springframework.stereotype.Component;

@Component
public class AttachmentMapper {

    public AttachmentDto toDto(Attachment entity) {
        if (entity == null) return null;
        return AttachmentDto.builder()
                .id(entity.getId())
                .fileName(entity.getFileName())
                .fileType(entity.getFileType())
                .fileSize(entity.getFileSize())
                .filePath(entity.getFilePath())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
