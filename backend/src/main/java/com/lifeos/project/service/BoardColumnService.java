package com.lifeos.project.service;

import com.lifeos.project.dto.BoardColumnDto;
import com.lifeos.project.dto.CreateColumnRequest;
import com.lifeos.project.dto.UpdateColumnRequest;
import java.util.List;
import java.util.UUID;

public interface BoardColumnService {
    BoardColumnDto createColumn(UUID boardId, CreateColumnRequest request);
    BoardColumnDto updateColumn(UUID id, UpdateColumnRequest request);
    void deleteColumn(UUID id);
    List<BoardColumnDto> getColumnsByBoard(UUID boardId);
}
