package com.lifeos.project.service;

import com.lifeos.project.dto.BoardDto;
import com.lifeos.project.dto.CreateBoardRequest;
import java.util.List;
import java.util.UUID;

public interface BoardService {
    BoardDto createBoard(UUID projectId, CreateBoardRequest request);
    BoardDto getBoardById(UUID id);
    List<BoardDto> getBoardsByProject(UUID projectId);
    BoardDto updateBoard(UUID id, CreateBoardRequest request);
    void deleteBoard(UUID id);
}
