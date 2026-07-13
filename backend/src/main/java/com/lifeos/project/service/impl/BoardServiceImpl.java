package com.lifeos.project.service.impl;

import com.lifeos.shared.kernel.exception.ApiException;
import com.lifeos.project.dto.BoardDto;
import com.lifeos.project.dto.CreateBoardRequest;
import com.lifeos.project.entity.Board;
import com.lifeos.project.entity.BoardColumn;
import com.lifeos.project.entity.Project;
import com.lifeos.project.entity.Task;
import com.lifeos.project.enums.ActivityAction;
import com.lifeos.project.mapper.BoardMapper;
import com.lifeos.project.repository.BoardRepository;
import com.lifeos.project.repository.ProjectRepository;
import com.lifeos.project.repository.TaskRepository;
import com.lifeos.project.service.BoardService;
import com.lifeos.project.service.ActivityLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BoardServiceImpl implements BoardService {

    private final BoardRepository boardRepository;
    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final BoardMapper boardMapper;
    private final ActivityLogService logService;

    @Override
    @Transactional
    public BoardDto createBoard(UUID projectId, CreateBoardRequest request) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ApiException("Project not found", HttpStatus.NOT_FOUND));

        Board board = Board.builder()
                .name(request.name())
                .description(request.description())
                .project(project)
                .build();

        Board saved = boardRepository.save(board);

        logService.log(project, saved, null, ActivityAction.CREATE_BOARD, 
                "Board '" + saved.getName() + "' created.");

        return boardMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BoardDto getBoardById(UUID id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ApiException("Board not found", HttpStatus.NOT_FOUND));
        return boardMapper.toDto(board);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BoardDto> getBoardsByProject(UUID projectId) {
        return boardRepository.findByProjectId(projectId).stream()
                .map(boardMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BoardDto updateBoard(UUID id, CreateBoardRequest request) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ApiException("Board not found", HttpStatus.NOT_FOUND));

        board.setName(request.name());
        board.setDescription(request.description());
        Board saved = boardRepository.save(board);

        logService.log(board.getProject(), saved, null, ActivityAction.UPDATE_BOARD, 
                "Board details updated.");

        return boardMapper.toDto(saved);
    }

    @Override
    @Transactional
    public void deleteBoard(UUID id) {
        Board board = boardRepository.findById(id)
                .orElseThrow(() -> new ApiException("Board not found", HttpStatus.NOT_FOUND));

        Instant now = Instant.now();
        board.setDeletedAt(now);
        for (BoardColumn col : board.getColumns()) {
            List<Task> tasks = taskRepository.findByColumnId(col.getId());
            for (Task task : tasks) {
                task.setDeletedAt(now);
                taskRepository.save(task);
            }
        }
        boardRepository.save(board);

        logService.log(board.getProject(), null, null, ActivityAction.DELETE_BOARD, 
                "Board '" + board.getName() + "' soft-deleted.");
    }
}
