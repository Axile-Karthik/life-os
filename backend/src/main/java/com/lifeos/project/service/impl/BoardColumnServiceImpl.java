package com.lifeos.project.service.impl;

import com.lifeos.shared.kernel.exception.ApiException;
import com.lifeos.project.dto.BoardColumnDto;
import com.lifeos.project.dto.CreateColumnRequest;
import com.lifeos.project.dto.UpdateColumnRequest;
import com.lifeos.project.entity.Board;
import com.lifeos.project.entity.BoardColumn;
import com.lifeos.project.enums.ActivityAction;
import com.lifeos.project.mapper.BoardColumnMapper;
import com.lifeos.project.repository.BoardColumnRepository;
import com.lifeos.project.repository.BoardRepository;
import com.lifeos.project.service.BoardColumnService;
import com.lifeos.project.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardColumnServiceImpl implements BoardColumnService {

    private final BoardColumnRepository columnRepository;
    private final BoardRepository boardRepository;
    private final BoardColumnMapper columnMapper;
    private final ActivityLogService logService;

    @Override
    @Transactional
    public BoardColumnDto createColumn(UUID boardId, CreateColumnRequest request) {
        Board board = boardRepository.findById(boardId)
                .orElseThrow(() -> new ApiException("Board not found", HttpStatus.NOT_FOUND));

        int position = columnRepository.findByBoardIdOrderByPositionAsc(boardId).size();

        BoardColumn column = BoardColumn.builder()
                .name(request.name())
                .position(position)
                .board(board)
                .build();

        BoardColumn saved = columnRepository.save(column);

        logService.log(board.getProject(), board, null, ActivityAction.CREATE_COLUMN, 
                "Column '" + saved.getName() + "' created.");

        return columnMapper.toDto(saved);
    }

    @Override
    @Transactional
    public BoardColumnDto updateColumn(UUID id, UpdateColumnRequest request) {
        BoardColumn column = columnRepository.findById(id)
                .orElseThrow(() -> new ApiException("Column not found", HttpStatus.NOT_FOUND));

        column.setName(request.name());

        if (request.position() != null && !request.position().equals(column.getPosition())) {
            UUID boardId = column.getBoard().getId();
            List<BoardColumn> columns = columnRepository.findByBoardIdOrderByPositionAsc(boardId);
            
            columns.remove(column);
            int newPos = request.position();
            if (newPos < 0) newPos = 0;
            if (newPos > columns.size()) newPos = columns.size();
            columns.add(newPos, column);

            for (int i = 0; i < columns.size(); i++) {
                columns.get(i).setPosition(i);
                columnRepository.save(columns.get(i));
            }
            
            logService.log(column.getBoard().getProject(), column.getBoard(), null, ActivityAction.MOVE_COLUMN, 
                    "Column '" + column.getName() + "' moved to position " + newPos);
        } else {
            columnRepository.save(column);
            logService.log(column.getBoard().getProject(), column.getBoard(), null, ActivityAction.UPDATE_COLUMN, 
                    "Column '" + column.getName() + "' updated.");
        }

        return columnMapper.toDto(column);
    }

    @Override
    @Transactional
    public void deleteColumn(UUID id) {
        BoardColumn column = columnRepository.findById(id)
                .orElseThrow(() -> new ApiException("Column not found", HttpStatus.NOT_FOUND));

        UUID boardId = column.getBoard().getId();
        logService.log(column.getBoard().getProject(), column.getBoard(), null, ActivityAction.DELETE_COLUMN, 
                "Column '" + column.getName() + "' deleted.");

        columnRepository.delete(column);

        List<BoardColumn> columns = columnRepository.findByBoardIdOrderByPositionAsc(boardId);
        for (int i = 0; i < columns.size(); i++) {
            columns.get(i).setPosition(i);
            columnRepository.save(columns.get(i));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardColumnDto> getColumnsByBoard(UUID boardId) {
        return columnRepository.findByBoardIdOrderByPositionAsc(boardId).stream()
                .map(columnMapper::toDto)
                .collect(Collectors.toList());
    }
}
