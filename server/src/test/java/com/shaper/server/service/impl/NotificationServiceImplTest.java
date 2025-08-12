package com.shaper.server.service.impl;

import com.shaper.server.model.dto.NotificationDto;
import com.shaper.server.model.entity.*;
import com.shaper.server.model.enums.NotificationType;
import com.shaper.server.model.enums.TaskType;
import com.shaper.server.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    @Mock
    private NotificationRepository notificationRepository;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private TaskRepository taskRepository;
    
    @Mock
    private HrUserRepository hrUserRepository;
    
    @InjectMocks
    private NotificationServiceImpl notificationService;
    
    private User testUser;
    private HrUser testHrUser;
    private Task testTask;
    private Notification testNotification;
    private UUID testUserId;
    
    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        
        testUser = mock(User.class);
        testUser.setId(testUserId);
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setEmail("john.doe@example.com");
        
        testHrUser = new HrUser();
        testHrUser.setId(UUID.randomUUID());
        testHrUser.setFirstName("Jane");
        testHrUser.setLastName("Smith");
        testHrUser.setEmail("jane.smith@example.com");
        
        testTask = new Task();
        testTask.setId(1);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setTaskType(TaskType.DOCUMENT);
        testTask.setRequiresSignature(true);
        
        testNotification = new Notification();
        testNotification.setId(1);
        testNotification.setUser(testUser);
        testNotification.setMessage("Test notification message");
        testNotification.setRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
        testNotification.setRelatedTask(testTask);
    }
    
    @Test
    void createNotification_ShouldCreateNotificationSuccessfully() {
        // Given
        String message = "Test notification";
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        NotificationDto result = notificationService.createNotification(testUserId, message, 1);
        
        // Then
        assertNotNull(result);
        assertEquals(testUserId, result.getUserId());
        assertEquals("John Doe", result.getUserName());
        assertEquals("Test notification message", result.getMessage());
        assertFalse(result.isRead());
        assertEquals(1, result.getRelatedTaskId());
        assertEquals("Test Task", result.getRelatedTaskTitle());
        
        verify(userRepository).findById(testUserId);
        verify(taskRepository).findById(1);
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void createNotification_ShouldThrowException_WhenUserNotFound() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            notificationService.createNotification(testUserId, "Test message", null));
        
        assertEquals("User not found with ID: " + testUserId, exception.getMessage());
        verify(userRepository).findById(testUserId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void createNotification_WithNotificationType_ShouldSetCorrectType() {
        // Given
        String message = "Test notification";
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        NotificationDto result = notificationService.createNotification(
            testUserId, message, 1, NotificationType.TASK_COMPLETED);
        
        // Then
        assertNotNull(result);
        assertEquals("TASK_COMPLETED", result.getNotificationType());
        
        verify(userRepository).findById(testUserId);
        verify(taskRepository).findById(1);
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void getNotificationsByUserId_ShouldReturnNotifications() {
        // Given
        List<Notification> notifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUser_IdOrderByCreatedAtDesc(testUserId))
            .thenReturn(notifications);
        
        // When
        List<NotificationDto> result = notificationService.getNotificationsByUserId(testUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test notification message", result.get(0).getMessage());
        
        verify(notificationRepository).findByUser_IdOrderByCreatedAtDesc(testUserId);
    }
    
    @Test
    void getUnreadNotificationsByUserId_ShouldReturnUnreadNotifications() {
        // Given
        List<Notification> unreadNotifications = Arrays.asList(testNotification);
        when(notificationRepository.findByUser_IdAndIsReadOrderByCreatedAtDesc(testUserId, false))
            .thenReturn(unreadNotifications);
        
        // When
        List<NotificationDto> result = notificationService.getUnreadNotificationsByUserId(testUserId);
        
        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertFalse(result.get(0).isRead());
        
        verify(notificationRepository).findByUser_IdAndIsReadOrderByCreatedAtDesc(testUserId, false);
    }
    
    @Test
    void markAsRead_ShouldMarkNotificationAsRead() {
        // Given
        testNotification.setRead(true);
        when(notificationRepository.findById(1)).thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        NotificationDto result = notificationService.markAsRead(1);
        
        // Then
        assertNotNull(result);
        assertTrue(result.isRead());
        
        verify(notificationRepository).findById(1);
        verify(notificationRepository).save(testNotification);
    }
    
    @Test
    void markAsRead_ShouldThrowException_WhenNotificationNotFound() {
        // Given
        when(notificationRepository.findById(1)).thenReturn(Optional.empty());
        
        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, () -> 
            notificationService.markAsRead(1));
        
        assertEquals("Notification not found with ID: 1", exception.getMessage());
        verify(notificationRepository).findById(1);
        verify(notificationRepository, never()).save(any(Notification.class));
    }
    
    @Test
    void markAllAsRead_ShouldMarkAllUnreadNotificationsAsRead() {
        // Given
        Notification notification1 = new Notification();
        notification1.setRead(false);
        Notification notification2 = new Notification();
        notification2.setRead(false);
        
        List<Notification> unreadNotifications = Arrays.asList(notification1, notification2);
        when(notificationRepository.findByUser_IdAndIsReadOrderByCreatedAtDesc(testUserId, false))
            .thenReturn(unreadNotifications);
        when(notificationRepository.saveAll(any())).thenReturn(unreadNotifications);
        
        // When
        notificationService.markAllAsRead(testUserId);
        
        // Then
        assertTrue(notification1.isRead());
        assertTrue(notification2.isRead());
        
        verify(notificationRepository).findByUser_IdAndIsReadOrderByCreatedAtDesc(testUserId, false);
        verify(notificationRepository).saveAll(unreadNotifications);
    }
    
    @Test
    void getUnreadCount_ShouldReturnCorrectCount() {
        // Given
        when(notificationRepository.countByUser_IdAndIsRead(testUserId, false)).thenReturn(5L);
        
        // When
        long result = notificationService.getUnreadCount(testUserId);
        
        // Then
        assertEquals(5L, result);
        verify(notificationRepository).countByUser_IdAndIsRead(testUserId, false);
    }
    
    @Test
    void createTaskCompletedNotification_ShouldNotifyHRManagers() {
        // Given
        List<HrUser> hrUsers = Arrays.asList(testHrUser);
        when(hrUserRepository.findAll()).thenReturn(hrUsers);
        when(userRepository.findById(testHrUser.getId())).thenReturn(Optional.of(testHrUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.createTaskCompletedNotification(testUser, testTask);
        
        // Then
        verify(hrUserRepository).findAll();
        verify(userRepository).findById(testHrUser.getId());
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void createDocumentSignedNotification_ShouldCreateNotification() {
        // Given
        when(userRepository.findById(testHrUser.getId())).thenReturn(Optional.of(testHrUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.createDocumentSignedNotification(testHrUser, testTask);
        
        // Then
        verify(userRepository).findById(testHrUser.getId());
        verify(taskRepository).findById(1);
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void createReminderNotification_ShouldCreateNotification() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.createReminderNotification(testUser, testTask);
        
        // Then
        verify(userRepository).findById(testUserId);
        verify(taskRepository).findById(1);
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void createSignatureRequestNotification_ShouldCreateNotification() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.createSignatureRequestNotification(testUser, testTask);
        
        // Then
        verify(userRepository).findById(testUserId);
        verify(taskRepository).findById(1);
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void createOverdueTaskNotification_ShouldCreateNotification() {
        // Given
        when(userRepository.findById(testUserId)).thenReturn(Optional.of(testUser));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.createOverdueTaskNotification(testUser, testTask);
        
        // Then
        verify(userRepository).findById(testUserId);
        verify(taskRepository).findById(1);
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void notifyHRManagersForTask_ShouldNotifyAllHRUsers() {
        // Given
        HrUser hrUser1 = new HrUser();
        hrUser1.setId(UUID.randomUUID());
        HrUser hrUser2 = new HrUser();
        hrUser2.setId(UUID.randomUUID());
        
        List<HrUser> hrUsers = Arrays.asList(hrUser1, hrUser2);
        when(hrUserRepository.findAll()).thenReturn(hrUsers);
        when(userRepository.findById(hrUser1.getId())).thenReturn(Optional.of(hrUser1));
        when(userRepository.findById(hrUser2.getId())).thenReturn(Optional.of(hrUser2));
        when(taskRepository.findById(1)).thenReturn(Optional.of(testTask));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.notifyHRManagersForTask(testTask, "Test message", NotificationType.TASK_COMPLETED);
        
        // Then
        verify(hrUserRepository).findAll();
        verify(userRepository, times(2)).findById(any(UUID.class));
        verify(taskRepository, times(2)).findById(1);
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }
    
    @Test
    void createOnboardingStartedNotification_ShouldCreateNotification() {
        // Given
        when(userRepository.findById(testHrUser.getId())).thenReturn(Optional.of(testHrUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.createOnboardingStartedNotification(testUser, testHrUser);
        
        // Then
        verify(userRepository).findById(testHrUser.getId());
        verify(notificationRepository).save(any(Notification.class));
    }
    
    @Test
    void createOnboardingCompletedNotification_ShouldCreateNotification() {
        // Given
        when(userRepository.findById(testHrUser.getId())).thenReturn(Optional.of(testHrUser));
        when(notificationRepository.save(any(Notification.class))).thenReturn(testNotification);
        
        // When
        notificationService.createOnboardingCompletedNotification(testUser, testHrUser);
        
        // Then
        verify(userRepository).findById(testHrUser.getId());
        verify(notificationRepository).save(any(Notification.class));
    }
}