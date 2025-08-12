package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.Template;
import com.shaper.server.model.enums.TaskStatus;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.repository.TaskRepository;
import com.shaper.server.repository.TemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceImplTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private TaskServiceImpl taskService;

    private Template testTemplate;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testTemplate = new Template();
        testTemplate.setId(1);
        testTemplate.setTitle("Test Template");

        testTask = new Task();
        testTask.setId(1);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskType(TaskType.DOCUMENT);
        testTask.setRequiresSignature(true);
        testTask.setTemplate(testTemplate);
        testTask.setOrderIndex(1);
        testTask.setStatus(TaskStatus.PENDING);
    }

    @Test
    void createTask_ShouldCreateTaskSuccessfully() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(taskRepository.findMaxOrderIndexByTemplateId(1)).thenReturn(0);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        // When
        Task result = taskService.createTask(1, "Test Task", "Test Description", 
                                           TaskType.DOCUMENT, true, null, null);

        // Then
        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        assertEquals("Test Description", result.getDescription());
        assertEquals(TaskType.DOCUMENT, result.getTaskType());
        assertTrue(result.isRequiresSignature());
        assertEquals(testTemplate, result.getTemplate());
        assertEquals(TaskStatus.PENDING, result.getStatus());

        verify(templateRepository).findById(1);
        verify(taskRepository).findMaxOrderIndexByTemplateId(1);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void createTask_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        when(templateRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            taskService.createTask(1, "Test Task", "Test Description", 
                                 TaskType.DOCUMENT, true, null, null));

        verify(templateRepository).findById(1);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void updateTask_ShouldUpdateTaskSuccessfully() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        LocalDateTime eventDate = LocalDateTime.now().plusDays(7);

        // When
        Task result = taskService.updateTask(1, "Updated Task", "Updated Description", 
                                           TaskType.EVENT, false, "http://example.com", eventDate);

        // Then
        assertNotNull(result);
        verify(taskRepository).findById(1);
        verify(taskRepository).save(testTask);
    }

    @Test
    void updateTask_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            taskService.updateTask(1, "Updated Task", "Updated Description", 
                                 TaskType.EVENT, false, null, null));

        verify(taskRepository).findById(1);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getTaskById_ShouldReturnTask_WhenTaskExists() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));

        // When
        Task result = taskService.getTaskById(1);

        // Then
        assertNotNull(result);
        assertEquals(testTask, result);
        verify(taskRepository).findById(1);
    }

    @Test
    void getTaskById_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> taskService.getTaskById(1));
        verify(taskRepository).findById(1);
    }

    @Test
    void getTasksByTemplateId_ShouldReturnOrderedTasks() {
        // Given
        Task task1 = new Task();
        task1.setId(1);
        task1.setOrderIndex(1);
        
        Task task2 = new Task();
        task2.setId(2);
        task2.setOrderIndex(2);

        List<Task> tasks = Arrays.asList(task1, task2);
        when(taskRepository.findByTemplate_IdOrderByOrderIndexAsc(1)).thenReturn(tasks);

        // When
        List<Task> result = taskService.getTasksByTemplateId(1);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1, result.get(0).getOrderIndex());
        assertEquals(2, result.get(1).getOrderIndex());
        verify(taskRepository).findByTemplate_IdOrderByOrderIndexAsc(1);
    }

    @Test
    void deleteTask_ShouldDeleteTaskAndReorderRemaining() {
        // Given
        testTask.setOrderIndex(2);
        
        Task task1 = new Task();
        task1.setId(2);
        task1.setOrderIndex(1);
        
        Task task3 = new Task();
        task3.setId(3);
        task3.setOrderIndex(3);

        List<Task> remainingTasks = Arrays.asList(task1, task3);

        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(taskRepository.findByTemplate_IdOrderByOrderIndexAsc(1)).thenReturn(remainingTasks);

        // When
        taskService.deleteTask(1);

        // Then
        verify(taskRepository).findById(1);
        verify(taskRepository).delete(testTask);
        verify(taskRepository).findByTemplate_IdOrderByOrderIndexAsc(1);
        verify(taskRepository).save(task3); // Should be reordered from 3 to 2
    }

    @Test
    void deleteTask_ShouldThrowException_WhenTaskNotFound() {
        // Given
        when(taskRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> taskService.deleteTask(1));
        verify(taskRepository).findById(1);
        verify(taskRepository, never()).delete(any(Task.class));
    }

    @Test
    void reorderTasks_ShouldUpdateOrderIndices() {
        // Given
        Task task1 = new Task();
        task1.setId(1);
        task1.setOrderIndex(1);
        task1.setTemplate(testTemplate);
        
        Task task2 = new Task();
        task2.setId(2);
        task2.setOrderIndex(2);
        task2.setTemplate(testTemplate);

        List<Task> templateTasks = Arrays.asList(task1, task2);
        List<Integer> newOrder = Arrays.asList(2, 1); // Reverse order

        when(taskRepository.findByTemplate_IdOrderByOrderIndexAsc(1)).thenReturn(templateTasks);
        when(taskRepository.findById(2)).thenReturn(Optional.of(task2));
        when(taskRepository.findById(1)).thenReturn(Optional.of(task1));

        // When
        taskService.reorderTasks(1, newOrder);

        // Then
        verify(taskRepository).findByTemplate_IdOrderByOrderIndexAsc(1);
        verify(taskRepository).findById(2);
        verify(taskRepository).findById(1);
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    @Test
    void reorderTasks_ShouldThrowException_WhenTaskCountMismatch() {
        // Given
        List<Task> templateTasks = Arrays.asList(testTask);
        List<Integer> newOrder = Arrays.asList(1, 2); // More tasks than exist

        when(taskRepository.findByTemplate_IdOrderByOrderIndexAsc(1)).thenReturn(templateTasks);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> 
            taskService.reorderTasks(1, newOrder));

        verify(taskRepository).findByTemplate_IdOrderByOrderIndexAsc(1);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void moveTaskUp_ShouldSwapWithTaskAbove() {
        // Given
        Task task1 = new Task();
        task1.setId(1);
        task1.setOrderIndex(1);
        
        testTask.setOrderIndex(2);

        List<Task> templateTasks = Arrays.asList(task1, testTask);

        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(taskRepository.findByTemplate_IdOrderByOrderIndexAsc(1)).thenReturn(templateTasks);

        // When
        taskService.moveTaskUp(1);

        // Then
        verify(taskRepository).findById(1);
        verify(taskRepository).findByTemplate_IdOrderByOrderIndexAsc(1);
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    @Test
    void moveTaskUp_ShouldDoNothing_WhenTaskIsAtTop() {
        // Given
        testTask.setOrderIndex(1);
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));

        // When
        taskService.moveTaskUp(1);

        // Then
        verify(taskRepository).findById(1);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void moveTaskDown_ShouldSwapWithTaskBelow() {
        // Given
        testTask.setOrderIndex(1);
        
        Task task2 = new Task();
        task2.setId(2);
        task2.setOrderIndex(2);

        List<Task> templateTasks = Arrays.asList(testTask, task2);

        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(taskRepository.countByTemplateId(1)).thenReturn(2L);
        when(taskRepository.findByTemplate_IdOrderByOrderIndexAsc(1)).thenReturn(templateTasks);

        // When
        taskService.moveTaskDown(1);

        // Then
        verify(taskRepository).findById(1);
        verify(taskRepository).countByTemplateId(1);
        verify(taskRepository).findByTemplate_IdOrderByOrderIndexAsc(1);
        verify(taskRepository, times(2)).save(any(Task.class));
    }

    @Test
    void moveTaskDown_ShouldDoNothing_WhenTaskIsAtBottom() {
        // Given
        testTask.setOrderIndex(2);
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(taskRepository.countByTemplateId(1)).thenReturn(2L);

        // When
        taskService.moveTaskDown(1);

        // Then
        verify(taskRepository).findById(1);
        verify(taskRepository).countByTemplateId(1);
        verify(taskRepository, never()).save(any(Task.class));
    }

    @Test
    void getNextOrderIndex_ShouldReturnOne_WhenNoTasksExist() {
        // Given
        when(taskRepository.findMaxOrderIndexByTemplateId(1)).thenReturn(null);

        // When
        Integer result = taskService.getNextOrderIndex(1);

        // Then
        assertEquals(1, result);
        verify(taskRepository).findMaxOrderIndexByTemplateId(1);
    }

    @Test
    void getNextOrderIndex_ShouldReturnMaxPlusOne_WhenTasksExist() {
        // Given
        when(taskRepository.findMaxOrderIndexByTemplateId(1)).thenReturn(3);

        // When
        Integer result = taskService.getNextOrderIndex(1);

        // Then
        assertEquals(4, result);
        verify(taskRepository).findMaxOrderIndexByTemplateId(1);
    }

    @Test
    void countTasksInTemplate_ShouldReturnCorrectCount() {
        // Given
        when(taskRepository.countByTemplateId(1)).thenReturn(5L);

        // When
        long result = taskService.countTasksInTemplate(1);

        // Then
        assertEquals(5L, result);
        verify(taskRepository).countByTemplateId(1);
    }
}