package com.shaper.server.controller;

import com.shaper.server.model.dto.*;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.enums.TaskStatus;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.service.TaskService;
import com.shaper.server.system.Result;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TaskControllerTest {

    @Mock
    private TaskService taskService;

    @InjectMocks
    private TaskController taskController;

    private Task testTask;
    private TaskDTO testTaskDTO;

    @BeforeEach
    void setUp() {
        // Create a mock template for the task
        com.shaper.server.model.entity.Template mockTemplate = new com.shaper.server.model.entity.Template();
        mockTemplate.setId(1);
        mockTemplate.setTitle("Test Template");
        
        testTask = new Task();
        testTask.setId(1);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskType(TaskType.DOCUMENT);
        testTask.setStatus(TaskStatus.PENDING);
        testTask.setRequiresSignature(false);
        testTask.setOrderIndex(1);
        testTask.setTemplate(mockTemplate);

        testTaskDTO = new TaskDTO();
        testTaskDTO.setId(1);
        testTaskDTO.setTitle("Test Task");
        testTaskDTO.setDescription("Test Description");
        testTaskDTO.setTaskType(TaskType.DOCUMENT);
        testTaskDTO.setStatus(TaskStatus.PENDING);
        testTaskDTO.setRequiresSignature(false);
        testTaskDTO.setOrderIndex(1);
        testTaskDTO.setTemplateId(1);
    }

    @Test
    void createTask_ValidRequest_ShouldReturnCreated() {
        Integer templateId = 1;
        CreateTaskRequest request = new CreateTaskRequest();
        request.setTitle("Complete Paperwork");
        request.setDescription("Fill out all required forms");
        request.setTaskType(TaskType.DOCUMENT);
        request.setRequiresSignature(true);

        when(taskService.createTask(eq(templateId), eq(request.getTitle()), eq(request.getDescription()),
                eq(request.getTaskType()), eq(request.isRequiresSignature()), 
                eq(request.getResourceUrl()), eq(request.getEventDate()))).thenReturn(testTask);

        ResponseEntity<Result> response = taskController.createTask(templateId, request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Task created successfully", response.getBody().getMessage());
        verify(taskService).createTask(eq(templateId), eq(request.getTitle()), eq(request.getDescription()),
                eq(request.getTaskType()), eq(request.isRequiresSignature()), 
                eq(request.getResourceUrl()), eq(request.getEventDate()));
    }

    @Test
    void getTaskById_ExistingTask_ShouldReturnTask() {
        Integer taskId = 1;
        when(taskService.getTaskById(taskId)).thenReturn(testTask);

        ResponseEntity<Result> response = taskController.getTaskById(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Task retrieved successfully", response.getBody().getMessage());
        verify(taskService).getTaskById(taskId);
    }

    @Test
    void updateTask_ValidRequest_ShouldReturnUpdated() {
        Integer taskId = 1;
        UpdateTaskRequest request = new UpdateTaskRequest();
        request.setTitle("Updated Task");
        request.setDescription("Updated description");
        request.setTaskType(TaskType.DOCUMENT);
        request.setRequiresSignature(false);

        // Create a mock template for the updated task
        com.shaper.server.model.entity.Template mockTemplate = new com.shaper.server.model.entity.Template();
        mockTemplate.setId(1);
        mockTemplate.setTitle("Test Template");
        
        Task updatedTask = new Task();
        updatedTask.setId(taskId);
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("Updated description");
        updatedTask.setTaskType(TaskType.DOCUMENT);
        updatedTask.setRequiresSignature(false);
        updatedTask.setTemplate(mockTemplate);

        when(taskService.updateTask(eq(taskId), eq(request.getTitle()), eq(request.getDescription()),
                eq(request.getTaskType()), eq(request.isRequiresSignature()), 
                eq(request.getResourceUrl()), eq(request.getEventDate()))).thenReturn(updatedTask);

        ResponseEntity<Result> response = taskController.updateTask(taskId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Task updated successfully", response.getBody().getMessage());
        verify(taskService).updateTask(eq(taskId), eq(request.getTitle()), eq(request.getDescription()),
                eq(request.getTaskType()), eq(request.isRequiresSignature()), 
                eq(request.getResourceUrl()), eq(request.getEventDate()));
    }

    @Test
    void deleteTask_ExistingTask_ShouldReturnSuccess() {
        Integer taskId = 1;
        doNothing().when(taskService).deleteTask(taskId);

        ResponseEntity<Result> response = taskController.deleteTask(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Task deleted successfully", response.getBody().getMessage());
        verify(taskService).deleteTask(taskId);
    }

    @Test
    void getTasksByTemplateId_ShouldReturnTasks() {
        Integer templateId = 1;
        List<Task> tasks = Arrays.asList(testTask);
        when(taskService.getTasksByTemplateId(templateId)).thenReturn(tasks);

        ResponseEntity<Result> response = taskController.getTasksByTemplateId(templateId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Tasks retrieved successfully", response.getBody().getMessage());
        verify(taskService).getTasksByTemplateId(templateId);
    }

    @Test
    void reorderTasks_ValidRequest_ShouldReturnSuccess() {
        ReorderTasksRequest request = new ReorderTasksRequest();
        request.setTemplateId(1);
        request.setTaskIds(Arrays.asList(2, 1)); // Reverse order

        doNothing().when(taskService).reorderTasks(request.getTemplateId(), request.getTaskIds());

        ResponseEntity<Result> response = taskController.reorderTasks(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Tasks reordered successfully", response.getBody().getMessage());
        verify(taskService).reorderTasks(request.getTemplateId(), request.getTaskIds());
    }

    @Test
    void moveTaskUp_ValidRequest_ShouldReturnSuccess() {
        Integer taskId = 2;
        doNothing().when(taskService).moveTaskUp(taskId);

        ResponseEntity<Result> response = taskController.moveTaskUp(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Task moved up successfully", response.getBody().getMessage());
        verify(taskService).moveTaskUp(taskId);
    }

    @Test
    void moveTaskDown_ValidRequest_ShouldReturnSuccess() {
        Integer taskId = 1;
        doNothing().when(taskService).moveTaskDown(taskId);

        ResponseEntity<Result> response = taskController.moveTaskDown(taskId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Task moved down successfully", response.getBody().getMessage());
        verify(taskService).moveTaskDown(taskId);
    }

    @Test
    void getTaskCount_ShouldReturnCount() {
        Integer templateId = 1;
        long count = 2L;
        when(taskService.countTasksInTemplate(templateId)).thenReturn(count);

        ResponseEntity<Result> response = taskController.getTaskCount(templateId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().isSuccess());
        assertEquals("Task count retrieved successfully", response.getBody().getMessage());
        assertEquals(count, response.getBody().getData());
        verify(taskService).countTasksInTemplate(templateId);
    }
}