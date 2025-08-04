package com.shaper.server.service.impl;

import com.shaper.server.model.dto.NotificationDto;
import com.shaper.server.model.entity.Notification;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.User;
import com.shaper.server.repository.NotificationRepository;
import com.shaper.server.repository.UserRepository;
import com.shaper.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public NotificationDto createNotification(UUID userId, String message, Integer relatedTaskId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setRead(false);
        
        if (relatedTaskId != null) {
            // Note: You might want to add TaskRepository injection and fetch the task
            // For now, we'll set the relatedTask to null or handle it differently
        }
        
        Notification savedNotification = notificationRepository.save(notification);
        return convertToDto(savedNotification);
    }
    
    @Override
    public List<NotificationDto> getNotificationsByUserId(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUser_IdOrderByCreatedAtDesc(userId);
        return notifications.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    public List<NotificationDto> getUnreadNotificationsByUserId(UUID userId) {
        List<Notification> notifications = notificationRepository.findByUser_IdAndIsReadOrderByCreatedAtDesc(userId, false);
        return notifications.stream().map(this::convertToDto).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public NotificationDto markAsRead(Integer id) {
        Notification notification = notificationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Notification not found"));
        
        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        return convertToDto(savedNotification);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUser_IdAndIsReadOrderByCreatedAtDesc(userId, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
    }
    
    @Override
    public long getUnreadCount(UUID userId) {
        return notificationRepository.countByUser_IdAndIsRead(userId, false);
    }
    
    @Override
    @Transactional
    public void createTaskCompletedNotification(User hire, Task task) {
        String message = String.format("Task '%s' has been completed by %s %s", 
            task.getTitle(), hire.getFirstName(), hire.getLastName());
        createNotification(hire.getId(), message, task.getId());
    }
    
    @Override
    @Transactional
    public void createDocumentSignedNotification(User hr, Task task) {
        String message = String.format("Document for task '%s' has been signed and requires your review", task.getTitle());
        createNotification(hr.getId(), message, task.getId());
    }
    
    @Override
    @Transactional
    public void createReminderNotification(User hire, Task task) {
        String message = String.format("Reminder: Task '%s' is due soon. Please complete it.", task.getTitle());
        createNotification(hire.getId(), message, task.getId());
    }
    
    private NotificationDto convertToDto(Notification notification) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setUserName(notification.getUser().getFirstName() + " " + notification.getUser().getLastName());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        
        if (notification.getRelatedTask() != null) {
            dto.setRelatedTaskId(notification.getRelatedTask().getId());
            dto.setRelatedTaskTitle(notification.getRelatedTask().getTitle());
        }
        
        // Determine notification type based on message content
        if (notification.getMessage().contains("completed")) {
            dto.setNotificationType("TASK_COMPLETED");
        } else if (notification.getMessage().contains("signed")) {
            dto.setNotificationType("DOCUMENT_SIGNED");
        } else if (notification.getMessage().contains("Reminder")) {
            dto.setNotificationType("REMINDER");
        } else {
            dto.setNotificationType("GENERAL");
        }
        
        return dto;
    }
}