package com.lifeos.project.service;

import com.lifeos.shared.kernel.exception.ApiException;
import com.lifeos.project.dto.*;
import com.lifeos.project.entity.*;
import com.lifeos.project.enums.*;
import com.lifeos.project.mapper.*;
import com.lifeos.project.repository.*;
import com.lifeos.project.service.impl.TaskServiceImpl;
import com.lifeos.shared.kernel.storage.StorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private BoardColumnRepository columnRepository;
    @Mock
    private LabelRepository labelRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private AttachmentRepository attachmentRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private TaskMapper taskMapper;
    @Mock
    private AttachmentMapper attachmentMapper;
    @Mock
    private ActivityLogService logService;
    @Mock
    private StorageService storageService;

    @InjectMocks
    private TaskServiceImpl taskService;

    private BoardColumn column;
    private Project project;
    private Board board;

    @BeforeEach
    void setUp() {
        project = Project.builder()
                .id(UUID.randomUUID())
                .name("Project")
                .ticketPrefix("PRJ")
                .nextTicketNumber(1L)
                .build();
        board = Board.builder().id(UUID.randomUUID()).name("Board").project(project).build();
        column = BoardColumn.builder().id(UUID.randomUUID()).name("Todo").position(0).board(board).build();
    }

    @Test
    void testCreateTask_Success() {
        CreateTaskRequest request = new CreateTaskRequest("New Task", "Task Description", TaskPriority.HIGH, null);

        when(columnRepository.findById(column.getId())).thenReturn(Optional.of(column));
        when(taskRepository.findByColumnIdAndParentIsNullOrderByPositionAsc(column.getId())).thenReturn(new ArrayList<>());
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        TaskDto mockDto = TaskDto.builder()
                .ticketKey("PRJ-1")
                .title("New Task")
                .position(0)
                .priority(TaskPriority.HIGH)
                .build();
        when(taskMapper.toDto(any(Task.class))).thenReturn(mockDto);

        TaskDto result = taskService.createTask(column.getId(), request);

        assertNotNull(result);
        assertEquals("New Task", result.title());
        assertEquals("PRJ-1", result.ticketKey());
        verify(columnRepository, times(1)).findById(column.getId());
        verify(projectRepository, times(1)).save(any(Project.class));
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(logService, times(1)).log(any(), any(), any(), eq(ActivityAction.CREATE_TASK), anyString());
    }

    @Test
    void testGetTaskById_NotFound() {
        UUID taskId = UUID.randomUUID();
        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> taskService.getTaskById(taskId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
        assertEquals("Task not found", exception.getMessage());
    }

    @Test
    void testDeleteTask_SoftDeleteSuccess() {
        UUID taskId = UUID.randomUUID();
        Task subtask = Task.builder().id(UUID.randomUUID()).title("Subtask").build();
        Task task = Task.builder()
                .id(taskId)
                .title("Task to delete")
                .column(column)
                .position(0)
                .subtasks(new ArrayList<>(java.util.List.of(subtask)))
                .build();

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(taskRepository.findByColumnIdAndParentIsNullOrderByPositionAsc(column.getId())).thenReturn(new ArrayList<>());

        taskService.deleteTask(taskId);

        assertNotNull(task.getDeletedAt());
        assertNotNull(subtask.getDeletedAt());
        verify(taskRepository, times(2)).save(any(Task.class));
        verify(logService, times(1)).log(any(), any(), any(), eq(ActivityAction.DELETE_TASK), anyString());
    }

    @Test
    void testCreateSubtask_Success() {
        UUID parentId = UUID.randomUUID();
        Task parent = Task.builder()
                .id(parentId)
                .ticketKey("PRJ-1")
                .column(column)
                .build();

        CreateTaskRequest request = new CreateTaskRequest("Subtask", "Sub description", TaskPriority.LOW, null);

        when(taskRepository.findById(parentId)).thenReturn(Optional.of(parent));
        when(projectRepository.save(any(Project.class))).thenReturn(project);
        when(taskRepository.save(any(Task.class))).thenAnswer(invocation -> {
            Task task = invocation.getArgument(0);
            task.setId(UUID.randomUUID());
            return task;
        });

        TaskDto mockDto = TaskDto.builder()
                .ticketKey("PRJ-2")
                .title("Subtask")
                .parentId(parentId)
                .build();
        when(taskMapper.toDto(any(Task.class))).thenReturn(mockDto);

        TaskDto result = taskService.createSubtask(parentId, request);

        assertNotNull(result);
        assertEquals("Subtask", result.title());
        assertEquals(parentId, result.parentId());
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    void testAddAttachment_Success() {
        UUID taskId = UUID.randomUUID();
        Task task = Task.builder()
                .id(taskId)
                .title("Task")
                .column(column)
                .build();

        org.springframework.web.multipart.MultipartFile file = mock(org.springframework.web.multipart.MultipartFile.class);
        when(file.getOriginalFilename()).thenReturn("test.txt");
        when(file.getContentType()).thenReturn("text/plain");
        when(file.getSize()).thenReturn(100L);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(task));
        when(storageService.store(file)).thenReturn("uuid-test.txt");

        Attachment attachment = Attachment.builder()
                .id(UUID.randomUUID())
                .fileName("test.txt")
                .filePath("uuid-test.txt")
                .build();
        when(attachmentRepository.save(any(Attachment.class))).thenReturn(attachment);

        AttachmentDto mockDto = AttachmentDto.builder()
                .fileName("test.txt")
                .filePath("uuid-test.txt")
                .build();
        when(attachmentMapper.toDto(any(Attachment.class))).thenReturn(mockDto);

        AttachmentDto result = taskService.addAttachment(taskId, file);

        assertNotNull(result);
        assertEquals("test.txt", result.fileName());
        assertEquals("uuid-test.txt", result.filePath());
        verify(storageService, times(1)).store(file);
        verify(attachmentRepository, times(1)).save(any(Attachment.class));
    }

    @Test
    void testDeleteAttachment_Success() {
        UUID attachmentId = UUID.randomUUID();
        Task task = Task.builder().column(column).build();
        Attachment attachment = Attachment.builder()
                .id(attachmentId)
                .fileName("test.txt")
                .filePath("uuid-test.txt")
                .task(task)
                .build();

        when(attachmentRepository.findById(attachmentId)).thenReturn(Optional.of(attachment));

        taskService.deleteAttachment(attachmentId);

        verify(storageService, times(1)).delete("uuid-test.txt");
        verify(attachmentRepository, times(1)).delete(attachment);
    }
}
