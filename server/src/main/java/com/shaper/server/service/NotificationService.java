package com.shaper.server.service;

import com.shaper.server.model.dto.NotificationDto;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.User;
import com.shaper.server.model.enums.NotificationType;

import java.util.List;
import java.util.UUID;

public interface NotificationService {
    
    NotificationDto createNotification(UUID userId, String message, Integer relatedTaskId);
    
    NotificationDto createNotification(UUID userId, String message, Integer relatedTaskId, NotificationType type);
    
    List<NotificationDto> getNotificationsByUserId(UUID userId);
    
    List<NotificationDto> getUnreadNotificationsByUserId(UUID userId);
    
    NotificationDto markAsRead(Integer id);
    
    void markAllAsRead(UUID userId);
    
    long getUnreadCount(UUID userId);
    
    void createTaskCompletedNotification(User hire, Task task);
    
    void createDocumentSignedNotification(User hr, Task task);
    
    void createReminderNotification(User hire, Task task);
    
    void createSignatureRequestNotification(User hire, Task task);
    
    void createOverdueTaskNotification(User hire, Task task);
    
    void notifyHRManagersForTask(Task task, String message, NotificationType type);
    
    void createOnboardingStartedNotification(User hire, User hrManager);
    
    void createOnboardingCompletedNotification(User hire, User hrManager);
}