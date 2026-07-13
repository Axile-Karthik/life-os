package com.lifeos.project.controller;

import com.lifeos.project.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class AttachmentController {

    private final TaskService taskService;

    @DeleteMapping("/api/attachments/{id}")
    public ResponseEntity<Void> deleteAttachment(@PathVariable UUID id) {
        taskService.deleteAttachment(id);
        return ResponseEntity.noContent().build();
    }
}
