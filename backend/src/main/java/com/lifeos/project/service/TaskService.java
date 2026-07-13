package com.lifeos.project.service;

import com.lifeos.project.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.UUID;

public interface TaskService {
    TaskDto createTask(UUID columnId, CreateTaskRequest request);
    TaskDto createSubtask(UUID parentTaskId, CreateTaskRequest request);
    TaskDto getTaskById(UUID id);
    Page<TaskDto> getTasks(UUID boardId, String priority, String search, Pageable pageable);
    TaskDto updateTask(UUID id, UpdateTaskRequest request);
    void deleteTask(UUID id);

    TaskDto moveTask(UUID id, MoveTaskRequest request);

    TaskDto addLabelToTask(UUID taskId, UUID labelId);
    TaskDto removeLabelFromTask(UUID taskId, UUID labelId);

    CommentDto addComment(UUID taskId, CreateCommentRequest request);
    void deleteComment(UUID commentId);

    AttachmentDto addAttachment(UUID taskId, org.springframework.web.multipart.MultipartFile file);
    void deleteAttachment(UUID attachmentId);
}
