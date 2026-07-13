package com.lifeos.project.controller;

import com.lifeos.project.dto.BoardDto;
import com.lifeos.project.dto.CreateBoardRequest;
import com.lifeos.project.service.BoardService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    @PostMapping("/api/projects/{projectId}/boards")
    public ResponseEntity<BoardDto> createBoard(@PathVariable UUID projectId, @Valid @RequestBody CreateBoardRequest request) {
        return ResponseEntity.ok(boardService.createBoard(projectId, request));
    }

    @GetMapping("/api/projects/{projectId}/boards")
    public ResponseEntity<List<BoardDto>> getBoardsByProject(@PathVariable UUID projectId) {
        return ResponseEntity.ok(boardService.getBoardsByProject(projectId));
    }

    @GetMapping("/api/boards/{id}")
    public ResponseEntity<BoardDto> getBoardById(@PathVariable UUID id) {
        return ResponseEntity.ok(boardService.getBoardById(id));
    }

    @PutMapping("/api/boards/{id}")
    public ResponseEntity<BoardDto> updateBoard(@PathVariable UUID id, @Valid @RequestBody CreateBoardRequest request) {
        return ResponseEntity.ok(boardService.updateBoard(id, request));
    }

    @DeleteMapping("/api/boards/{id}")
    public ResponseEntity<Void> deleteBoard(@PathVariable UUID id) {
        boardService.deleteBoard(id);
        return ResponseEntity.noContent().build();
    }
}
