package com.shaper.server.service.impl;

import com.shaper.server.exception.DataNotFoundException;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.TodoStatus;
import com.shaper.server.repository.HireRepository;
import com.shaper.server.repository.ProgressRepository;
import com.shaper.server.repository.TemplateRepository;
import com.shaper.server.repository.TodoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProgressServiceImplTest {

    @Mock
    private ProgressRepository progressRepository;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private HireRepository hireRepository;

    @Mock
    private TemplateRepository templateRepository;

    @InjectMocks
    private ProgressServiceImpl progressService;

    private Hire testHire;
    private Template testTemplate;
    private Progress testProgress;
    private Todo testTodo;
    private UUID hireId;

    @BeforeEach
    void setUp() {
        hireId = UUID.randomUUID();

        testHire = new Hire();
        testHire.setId(hireId);
        testHire.setFirstName("John");
        testHire.setLastName("Doe");

        testTemplate = new Template();
        testTemplate.setId(1);
        testTemplate.setTitle("Test Template");
        
        // Create tasks for the template
        Set<Task> tasks = new HashSet<>();
        for (int i = 1; i <= 3; i++) {
            Task task = new Task();
            task.setId(i);
            task.setTitle("Task " + i);
            task.setTemplate(testTemplate);
            tasks.add(task);
        }
        testTemplate.setTasks(tasks);

        testProgress = new Progress();
        testProgress.setId(1);
        testProgress.setHire(testHire);
        testProgress.setTemplate(testTemplate);
        testProgress.setTotalTasks(3);
        testProgress.setCompletedTasks(1);
        testProgress.setCompletionPercentage(33.33);
        testProgress.setLastUpdated(LocalDateTime.now());

        testTodo = new Todo();
        testTodo.setId(1);
        testTodo.setHire(testHire);
        testTodo.setTemplate(testTemplate);
        testTodo.setStatus(TodoStatus.COMPLETED);
    }

    @Test
    void initializeProgress_ShouldCreateNewProgress_WhenNotExists() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.empty());
        when(hireRepository.findById(hireId)).thenReturn(Optional.of(testHire));
        when(templateRepository.findById(1)).thenReturn(Optional.of(testTemplate));
        when(progressRepository.save(any(Progress.class))).thenReturn(testProgress);

        // When
        Progress result = progressService.initializeProgress(hireId, 1);

        // Then
        assertNotNull(result);
        verify(progressRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(hireRepository).findById(hireId);
        verify(templateRepository).findById(1);
        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    void initializeProgress_ShouldReturnExisting_WhenAlreadyExists() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.of(testProgress));

        // When
        Progress result = progressService.initializeProgress(hireId, 1);

        // Then
        assertNotNull(result);
        assertEquals(testProgress, result);
        verify(progressRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    void initializeProgress_ShouldThrowException_WhenHireNotFound() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.empty());
        when(hireRepository.findById(hireId)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            progressService.initializeProgress(hireId, 1));

        verify(hireRepository).findById(hireId);
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    void initializeProgress_ShouldThrowException_WhenTemplateNotFound() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.empty());
        when(hireRepository.findById(hireId)).thenReturn(Optional.of(testHire));
        when(templateRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            progressService.initializeProgress(hireId, 1));

        verify(templateRepository).findById(1);
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    void updateProgressOnTodoCompletion_ShouldRecalculateProgress() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.of(testTodo));
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.of(testProgress));
        
        List<Todo> allTodos = Arrays.asList(
            createTodo(1, TodoStatus.COMPLETED),
            createTodo(2, TodoStatus.COMPLETED),
            createTodo(3, TodoStatus.PENDING)
        );
        when(todoRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(allTodos);
        when(progressRepository.save(any(Progress.class))).thenReturn(testProgress);

        // When
        Progress result = progressService.updateProgressOnTodoCompletion(1);

        // Then
        assertNotNull(result);
        verify(todoRepository).findById(1);
        verify(progressRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(todoRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(progressRepository).save(any(Progress.class));
    }

    @Test
    void updateProgressOnTodoCompletion_ShouldThrowException_WhenTodoNotFound() {
        // Given
        when(todoRepository.findById(1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            progressService.updateProgressOnTodoCompletion(1));

        verify(todoRepository).findById(1);
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    void recalculateProgress_ShouldUpdateCompletionPercentage() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.of(testProgress));
        
        List<Todo> allTodos = Arrays.asList(
            createTodo(1, TodoStatus.COMPLETED),
            createTodo(2, TodoStatus.COMPLETED),
            createTodo(3, TodoStatus.PENDING)
        );
        when(todoRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(allTodos);
        when(progressRepository.save(any(Progress.class))).thenReturn(testProgress);

        // When
        Progress result = progressService.recalculateProgress(hireId, 1);

        // Then
        assertNotNull(result);
        verify(progressRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(todoRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(progressRepository).save(testProgress);
    }

    @Test
    void recalculateProgress_ShouldThrowException_WhenProgressNotFound() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            progressService.recalculateProgress(hireId, 1));

        verify(progressRepository).findByHireIdAndTemplateId(hireId, 1);
        verify(progressRepository, never()).save(any(Progress.class));
    }

    @Test
    void getProgress_ShouldReturnProgress_WhenExists() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.of(testProgress));

        // When
        Progress result = progressService.getProgress(hireId, 1);

        // Then
        assertNotNull(result);
        assertEquals(testProgress, result);
        verify(progressRepository).findByHireIdAndTemplateId(hireId, 1);
    }

    @Test
    void getProgress_ShouldThrowException_WhenNotFound() {
        // Given
        when(progressRepository.findByHireIdAndTemplateId(hireId, 1)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(DataNotFoundException.class, () -> 
            progressService.getProgress(hireId, 1));

        verify(progressRepository).findByHireIdAndTemplateId(hireId, 1);
    }

    @Test
    void getProgressByHire_ShouldReturnAllProgressForHire() {
        // Given
        List<Progress> progressList = Arrays.asList(testProgress);
        when(progressRepository.findByHire_Id(hireId)).thenReturn(progressList);

        // When
        List<Progress> result = progressService.getProgressByHire(hireId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(testProgress, result.get(0));
        verify(progressRepository).findByHire_Id(hireId);
    }

    @Test
    void getProgressByHrUser_ShouldReturnProgressForHrUser() {
        // Given
        UUID hrId = UUID.randomUUID();
        List<Progress> progressList = Arrays.asList(testProgress);
        when(progressRepository.findByHrId(hrId)).thenReturn(progressList);

        // When
        List<Progress> result = progressService.getProgressByHrUser(hrId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(progressRepository).findByHrId(hrId);
    }

    @Test
    void getProgressByCompany_ShouldReturnProgressForCompany() {
        // Given
        List<Progress> progressList = Arrays.asList(testProgress);
        when(progressRepository.findByCompanyId(1)).thenReturn(progressList);

        // When
        List<Progress> result = progressService.getProgressByCompany(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(progressRepository).findByCompanyId(1);
    }

    @Test
    void getProgressByDepartment_ShouldReturnProgressForDepartment() {
        // Given
        List<Progress> progressList = Arrays.asList(testProgress);
        when(progressRepository.findByDepartmentId(1)).thenReturn(progressList);

        // When
        List<Progress> result = progressService.getProgressByDepartment(1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(progressRepository).findByDepartmentId(1);
    }

    @Test
    void calculateOverallCompletionPercentage_ShouldReturnZero_WhenNoProgress() {
        // Given
        when(progressRepository.findByHire_Id(hireId)).thenReturn(Collections.emptyList());

        // When
        Double result = progressService.calculateOverallCompletionPercentage(hireId);

        // Then
        assertEquals(0.0, result);
        verify(progressRepository).findByHire_Id(hireId);
    }

    @Test
    void calculateOverallCompletionPercentage_ShouldCalculateWeightedAverage() {
        // Given
        Progress progress1 = new Progress();
        progress1.setTotalTasks(4);
        progress1.setCompletionPercentage(50.0);

        Progress progress2 = new Progress();
        progress2.setTotalTasks(2);
        progress2.setCompletionPercentage(100.0);

        List<Progress> progressList = Arrays.asList(progress1, progress2);
        when(progressRepository.findByHire_Id(hireId)).thenReturn(progressList);

        // When
        Double result = progressService.calculateOverallCompletionPercentage(hireId);

        // Then
        // Expected: (50.0 * 4 + 100.0 * 2) / (4 + 2) = 400 / 6 = 66.67
        assertEquals(66.66666666666667, result, 0.001);
        verify(progressRepository).findByHire_Id(hireId);
    }

    @Test
    void getAverageCompletionByHrUser_ShouldReturnAverageFromRepository() {
        // Given
        UUID hrId = UUID.randomUUID();
        when(progressRepository.getAverageCompletionByHrId(hrId)).thenReturn(75.5);

        // When
        Double result = progressService.getAverageCompletionByHrUser(hrId);

        // Then
        assertEquals(75.5, result);
        verify(progressRepository).getAverageCompletionByHrId(hrId);
    }

    @Test
    void countCompletedOnboardingByHrUser_ShouldReturnCountFromRepository() {
        // Given
        UUID hrId = UUID.randomUUID();
        when(progressRepository.countCompletedByHrId(hrId)).thenReturn(5L);

        // When
        long result = progressService.countCompletedOnboardingByHrUser(hrId);

        // Then
        assertEquals(5L, result);
        verify(progressRepository).countCompletedByHrId(hrId);
    }

    @Test
    void getAtRiskProgress_ShouldReturnProgressBelowThreshold() {
        // Given
        UUID hrId = UUID.randomUUID();
        
        Progress atRiskProgress = new Progress();
        atRiskProgress.setCompletionPercentage(30.0);
        atRiskProgress.setCreatedAt(LocalDateTime.now().minusDays(10));

        Progress onTrackProgress = new Progress();
        onTrackProgress.setCompletionPercentage(80.0);
        onTrackProgress.setCreatedAt(LocalDateTime.now().minusDays(10));

        Progress recentProgress = new Progress();
        recentProgress.setCompletionPercentage(20.0);
        recentProgress.setCreatedAt(LocalDateTime.now().minusDays(3));

        List<Progress> allProgress = Arrays.asList(atRiskProgress, onTrackProgress, recentProgress);
        when(progressRepository.findByHrId(hrId)).thenReturn(allProgress);

        // When
        List<Progress> result = progressService.getAtRiskProgress(hrId);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(atRiskProgress, result.get(0));
        verify(progressRepository).findByHrId(hrId);
    }

    private Todo createTodo(Integer id, TodoStatus status) {
        Todo todo = new Todo();
        todo.setId(id);
        todo.setHire(testHire);
        todo.setTemplate(testTemplate);
        todo.setStatus(status);
        return todo;
    }
}