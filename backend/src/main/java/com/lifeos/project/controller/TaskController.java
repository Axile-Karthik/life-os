package com.lifeos.project.controller;

import com.lifeos.project.dto.*;
import com.lifeos.project.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @PostMapping("/api/columns/{columnId}/tasks")
    public ResponseEntity<TaskDto> createTask(@PathVariable UUID columnId, @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createTask(columnId, request));
    }

    @PostMapping("/api/tasks/{parentId}/subtasks")
    public ResponseEntity<TaskDto> createSubtask(@PathVariable UUID parentId, @Valid @RequestBody CreateTaskRequest request) {
        return ResponseEntity.ok(taskService.createSubtask(parentId, request));
    }

    @GetMapping("/api/boards/{boardId}/tasks")
    public ResponseEntity<Page<TaskDto>> getTasks(
            @PathVariable UUID boardId,
            @RequestParam(required = false) String priority,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(defaultValue = "position") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        return ResponseEntity.ok(taskService.getTasks(boardId, priority, search, PageRequest.of(page, size, sort)));
    }

    @GetMapping("/api/tasks/{id}")
    public ResponseEntity<TaskDto> getTaskById(@PathVariable UUID id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PutMapping("/api/tasks/{id}")
    public ResponseEntity<TaskDto> updateTask(@PathVariable UUID id, @Valid @RequestBody UpdateTaskRequest request) {
        return ResponseEntity.ok(taskService.updateTask(id, request));
    }

    @DeleteMapping("/api/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/api/tasks/{id}/move")
    public ResponseEntity<TaskDto> moveTask(@PathVariable UUID id, @Valid @RequestBody MoveTaskRequest request) {
        return ResponseEntity.ok(taskService.moveTask(id, request));
    }

    @PostMapping("/api/tasks/{id}/labels/{labelId}")
    public ResponseEntity<TaskDto> addLabelToTask(@PathVariable UUID id, @PathVariable UUID labelId) {
        return ResponseEntity.ok(taskService.addLabelToTask(id, labelId));
    }

    @DeleteMapping("/api/tasks/{id}/labels/{labelId}")
    public ResponseEntity<TaskDto> removeLabelFromTask(@PathVariable UUID id, @PathVariable UUID labelId) {
        return ResponseEntity.ok(taskService.removeLabelFromTask(id, labelId));
    }

    @PostMapping("/api/tasks/{id}/comments")
    public ResponseEntity<CommentDto> addComment(@PathVariable UUID id, @Valid @RequestBody CreateCommentRequest request) {
        return ResponseEntity.ok(taskService.addComment(id, request));
    }

    @PostMapping(value = "/api/tasks/{id}/attachments", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AttachmentDto> addAttachment(@PathVariable UUID id, @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        return ResponseEntity.ok(taskService.addAttachment(id, file));
    }
}
