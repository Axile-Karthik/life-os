package com.lifeos.project.controller;

import com.lifeos.project.dto.ActivityLogDto;
import com.lifeos.project.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ActivityLogController {

    private final ActivityLogService logService;

    @GetMapping("/api/projects/{projectId}/activity")
    public ResponseEntity<Page<ActivityLogDto>> getProjectActivity(
            @PathVariable UUID projectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        return ResponseEntity.ok(logService.getProjectActivity(projectId, PageRequest.of(page, size, Sort.by("createdAt").descending())));
    }
}
