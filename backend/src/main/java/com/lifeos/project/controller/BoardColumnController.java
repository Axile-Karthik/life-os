package com.lifeos.project.controller;

import com.lifeos.project.dto.BoardColumnDto;
import com.lifeos.project.dto.CreateColumnRequest;
import com.lifeos.project.dto.UpdateColumnRequest;
import com.lifeos.project.service.BoardColumnService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BoardColumnController {

    private final BoardColumnService columnService;

    @PostMapping("/api/boards/{boardId}/columns")
    public ResponseEntity<BoardColumnDto> createColumn(@PathVariable UUID boardId, @Valid @RequestBody CreateColumnRequest request) {
        return ResponseEntity.ok(columnService.createColumn(boardId, request));
    }

    @GetMapping("/api/boards/{boardId}/columns")
    public ResponseEntity<List<BoardColumnDto>> getColumnsByBoard(@PathVariable UUID boardId) {
        return ResponseEntity.ok(columnService.getColumnsByBoard(boardId));
    }

    @PutMapping("/api/columns/{id}")
    public ResponseEntity<BoardColumnDto> updateColumn(@PathVariable UUID id, @Valid @RequestBody UpdateColumnRequest request) {
        return ResponseEntity.ok(columnService.updateColumn(id, request));
    }

    @DeleteMapping("/api/columns/{id}")
    public ResponseEntity<Void> deleteColumn(@PathVariable UUID id) {
        columnService.deleteColumn(id);
        return ResponseEntity.noContent().build();
    }
}
