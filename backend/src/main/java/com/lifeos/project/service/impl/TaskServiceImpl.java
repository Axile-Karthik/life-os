package com.lifeos.project.service.impl;

import com.lifeos.shared.kernel.exception.ApiException;
import com.lifeos.project.dto.*;
import com.lifeos.project.entity.*;
import com.lifeos.project.enums.ActivityAction;
import com.lifeos.project.mapper.CommentMapper;
import com.lifeos.project.mapper.AttachmentMapper;
import com.lifeos.project.mapper.TaskMapper;
import com.lifeos.project.repository.*;
import com.lifeos.project.service.TaskService;
import com.lifeos.project.service.ActivityLogService;
import com.lifeos.shared.kernel.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final BoardColumnRepository columnRepository;
    private final LabelRepository labelRepository;
    private final CommentRepository commentRepository;
    private final AttachmentRepository attachmentRepository;
    private final ProjectRepository projectRepository;
    private final TaskMapper taskMapper;
    private final CommentMapper commentMapper;
    private final AttachmentMapper attachmentMapper;
    private final ActivityLogService logService;
    private final StorageService storageService;

    @Override
    @Transactional
    public TaskDto createTask(UUID columnId, CreateTaskRequest request) {
        BoardColumn column = columnRepository.findById(columnId)
                .orElseThrow(() -> new ApiException("Column not found", HttpStatus.NOT_FOUND));

        Project project = column.getBoard().getProject();
        String ticketKey = project.getTicketPrefix() + "-" + project.getNextTicketNumber();
        project.setNextTicketNumber(project.getNextTicketNumber() + 1);
        projectRepository.save(project);

        int position = taskRepository.findByColumnIdAndParentIsNullOrderByPositionAsc(columnId).size();

        Task task = Task.builder()
                .ticketKey(ticketKey)
                .title(request.title())
                .description(request.description())
                .column(column)
                .position(position)
                .priority(request.priority())
                .dueDate(request.dueDate())
                .build();

        Task saved = taskRepository.save(task);

        logService.log(column.getBoard().getProject(), column.getBoard(), saved, 
                ActivityAction.CREATE_TASK, "Task '" + saved.getTitle() + "' created with key " + ticketKey);

        return taskMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TaskDto createSubtask(UUID parentTaskId, CreateTaskRequest request) {
        Task parent = taskRepository.findById(parentTaskId)
                .orElseThrow(() -> new ApiException("Parent task not found", HttpStatus.NOT_FOUND));

        BoardColumn column = parent.getColumn();
        Project project = column.getBoard().getProject();
        String ticketKey = project.getTicketPrefix() + "-" + project.getNextTicketNumber();
        project.setNextTicketNumber(project.getNextTicketNumber() + 1);
        projectRepository.save(project);

        int position = parent.getSubtasks().size();

        Task subtask = Task.builder()
                .ticketKey(ticketKey)
                .title(request.title())
                .description(request.description())
                .column(column)
                .parent(parent)
                .position(position)
                .priority(request.priority())
                .dueDate(request.dueDate())
                .build();

        Task saved = taskRepository.save(subtask);

        logService.log(column.getBoard().getProject(), column.getBoard(), saved, 
                ActivityAction.CREATE_TASK, "Subtask '" + saved.getTitle() + "' created for parent task " + parent.getTicketKey());

        return taskMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public TaskDto getTaskById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));
        return taskMapper.toDto(task);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TaskDto> getTasks(UUID boardId, String priority, String search, Pageable pageable) {
        if (search != null && !search.trim().isEmpty()) {
            return taskRepository.fullTextSearch(boardId, priority, search.trim(), pageable)
                    .map(taskMapper::toDto);
        }
        return taskRepository.findFilteredAndPaged(boardId, priority, null, pageable)
                .map(taskMapper::toDto);
    }

    @Override
    @Transactional
    public TaskDto updateTask(UUID id, UpdateTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        task.setTitle(request.title());
        task.setDescription(request.description());
        task.setPriority(request.priority());
        task.setDueDate(request.dueDate());

        if (request.columnId() != null && !request.columnId().equals(task.getColumn().getId())) {
            UUID targetColumnId = request.columnId();
            MoveTaskRequest moveRequest = new MoveTaskRequest(targetColumnId, 0);
            moveTaskInternal(task, moveRequest);
        } else {
            taskRepository.save(task);
        }

        logService.log(task.getColumn().getBoard().getProject(), task.getColumn().getBoard(), task, 
                ActivityAction.UPDATE_TASK, "Task details updated.");

        return taskMapper.toDto(task);
    }

    @Override
    @Transactional
    public void deleteTask(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        UUID columnId = task.getColumn().getId();
        logService.log(task.getColumn().getBoard().getProject(), task.getColumn().getBoard(), null, 
                ActivityAction.DELETE_TASK, "Task '" + task.getTitle() + "' soft-deleted.");

        Instant now = Instant.now();
        task.setDeletedAt(now);
        for (Task subtask : task.getSubtasks()) {
            subtask.setDeletedAt(now);
            taskRepository.save(subtask);
        }
        taskRepository.save(task);

        List<Task> remainingTasks = taskRepository.findByColumnIdAndParentIsNullOrderByPositionAsc(columnId);
        for (int i = 0; i < remainingTasks.size(); i++) {
            remainingTasks.get(i).setPosition(i);
            taskRepository.save(remainingTasks.get(i));
        }
    }

    @Override
    @Transactional
    public TaskDto moveTask(UUID id, MoveTaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        moveTaskInternal(task, request);

        return taskMapper.toDto(task);
    }

    private void moveTaskInternal(Task task, MoveTaskRequest request) {
        UUID sourceColumnId = task.getColumn().getId();
        UUID targetColumnId = request.targetColumnId();

        BoardColumn sourceColumn = columnRepository.findById(sourceColumnId)
                .orElseThrow(() -> new ApiException("Source column not found", HttpStatus.NOT_FOUND));
        
        if (sourceColumnId.equals(targetColumnId)) {
            List<Task> tasks = taskRepository.findByColumnIdAndParentIsNullOrderByPositionAsc(sourceColumnId);
            tasks.remove(task);
            
            int newIdx = request.newIndex();
            if (newIdx < 0) newIdx = 0;
            if (newIdx > tasks.size()) newIdx = tasks.size();
            tasks.add(newIdx, task);

            for (int i = 0; i < tasks.size(); i++) {
                tasks.get(i).setPosition(i);
                taskRepository.save(tasks.get(i));
            }
            logService.log(sourceColumn.getBoard().getProject(), sourceColumn.getBoard(), task, 
                    ActivityAction.MOVE_TASK, "Task '" + task.getTitle() + "' reordered to position " + newIdx + " in column " + sourceColumn.getName());
        } else {
            BoardColumn targetColumn = columnRepository.findById(targetColumnId)
                    .orElseThrow(() -> new ApiException("Target column not found", HttpStatus.NOT_FOUND));

            List<Task> sourceTasks = taskRepository.findByColumnIdAndParentIsNullOrderByPositionAsc(sourceColumnId);
            List<Task> targetTasks = taskRepository.findByColumnIdAndParentIsNullOrderByPositionAsc(targetColumnId);

            sourceTasks.remove(task);
            for (int i = 0; i < sourceTasks.size(); i++) {
                sourceTasks.get(i).setPosition(i);
                taskRepository.save(sourceTasks.get(i));
            }

            task.setColumn(targetColumn);

            int newIdx = request.newIndex();
            if (newIdx < 0) newIdx = 0;
            if (newIdx > targetTasks.size()) newIdx = targetTasks.size();
            targetTasks.add(newIdx, task);

            for (int i = 0; i < targetTasks.size(); i++) {
                targetTasks.get(i).setPosition(i);
                taskRepository.save(targetTasks.get(i));
            }
            logService.log(sourceColumn.getBoard().getProject(), sourceColumn.getBoard(), task, 
                    ActivityAction.MOVE_TASK, "Task '" + task.getTitle() + "' moved from column '" + sourceColumn.getName() + "' to '" + targetColumn.getName() + "' at position " + newIdx);
        }
    }

    @Override
    @Transactional
    public TaskDto addLabelToTask(UUID taskId, UUID labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ApiException("Label not found", HttpStatus.NOT_FOUND));

        if (!label.getProject().getId().equals(task.getColumn().getBoard().getProject().getId())) {
            throw new ApiException("Label does not belong to the same project", HttpStatus.BAD_REQUEST);
        }

        task.getLabels().add(label);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    @Transactional
    public TaskDto removeLabelFromTask(UUID taskId, UUID labelId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        Label label = labelRepository.findById(labelId)
                .orElseThrow(() -> new ApiException("Label not found", HttpStatus.NOT_FOUND));

        task.getLabels().remove(label);
        Task saved = taskRepository.save(task);
        return taskMapper.toDto(saved);
    }

    @Override
    @Transactional
    public CommentDto addComment(UUID taskId, CreateCommentRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        Comment comment = Comment.builder()
                .content(request.content())
                .task(task)
                .build();

        Comment saved = commentRepository.save(comment);

        logService.log(task.getColumn().getBoard().getProject(), task.getColumn().getBoard(), task, 
                ActivityAction.ADD_COMMENT, "Comment added to task.");

        return commentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteComment(UUID commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new ApiException("Comment not found", HttpStatus.NOT_FOUND));

        logService.log(comment.getTask().getColumn().getBoard().getProject(), 
                comment.getTask().getColumn().getBoard(), comment.getTask(), 
                ActivityAction.DELETE_COMMENT, "Comment deleted.");

        commentRepository.delete(comment);
    }

    @Override
    @Transactional
    public String toString() {
        return "TaskServiceImpl{}";
    }

    @Override
    @Transactional
    public AttachmentDto addAttachment(UUID taskId, org.springframework.web.multipart.MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ApiException("Task not found", HttpStatus.NOT_FOUND));

        String storedFilename = storageService.store(file);

        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .fileSize(file.getSize())
                .filePath(storedFilename)
                .task(task)
                .build();

        Attachment saved = attachmentRepository.save(attachment);

        logService.log(task.getColumn().getBoard().getProject(), task.getColumn().getBoard(), task, 
                ActivityAction.ADD_ATTACHMENT, "Attachment '" + saved.getFileName() + "' added to task.");

        return attachmentMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteAttachment(UUID attachmentId) {
        Attachment attachment = attachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new ApiException("Attachment not found", HttpStatus.NOT_FOUND));

        logService.log(attachment.getTask().getColumn().getBoard().getProject(), 
                attachment.getTask().getColumn().getBoard(), attachment.getTask(), 
                ActivityAction.DELETE_ATTACHMENT, "Attachment '" + attachment.getFileName() + "' deleted.");

        storageService.delete(attachment.getFilePath());

        attachmentRepository.delete(attachment);
    }
}
