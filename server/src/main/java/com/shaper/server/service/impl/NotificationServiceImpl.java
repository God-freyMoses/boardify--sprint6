package com.shaper.server.service.impl;

import com.shaper.server.model.dto.NotificationDto;
import com.shaper.server.model.entity.Notification;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.User;
import com.shaper.server.model.entity.HrUser;
import com.shaper.server.model.enums.NotificationType;
import com.shaper.server.repository.NotificationRepository;
import com.shaper.server.repository.UserRepository;
import com.shaper.server.repository.TaskRepository;
import com.shaper.server.repository.HrUserRepository;
import com.shaper.server.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final HrUserRepository hrUserRepository;
    
    @Override
    @Transactional
    public NotificationDto createNotification(UUID userId, String message, Integer relatedTaskId) {
        return createNotification(userId, message, relatedTaskId, NotificationType.GENERAL);
    }
    
    @Override
    @Transactional
    public NotificationDto createNotification(UUID userId, String message, Integer relatedTaskId, NotificationType type) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));
        
        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setRead(false);
        
        if (relatedTaskId != null) {
            Task task = taskRepository.findById(relatedTaskId)
                .orElseThrow(() -> new RuntimeException("Task not found with ID: " + relatedTaskId));
            notification.setRelatedTask(task);
        }
        
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Created notification for user {} with type {}: {}", userId, type, message);
        return convertToDto(savedNotification, type);
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
            .orElseThrow(() -> new RuntimeException("Notification not found with ID: " + id));
        
        notification.setRead(true);
        Notification savedNotification = notificationRepository.save(notification);
        log.info("Marked notification {} as read for user {}", id, notification.getUser().getId());
        return convertToDto(savedNotification);
    }
    
    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        List<Notification> unreadNotifications = notificationRepository.findByUser_IdAndIsReadOrderByCreatedAtDesc(userId, false);
        unreadNotifications.forEach(notification -> notification.setRead(true));
        notificationRepository.saveAll(unreadNotifications);
        log.info("Marked {} notifications as read for user {}", unreadNotifications.size(), userId);
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
        
        // Notify HR managers about task completion
        notifyHRManagersForTask(task, message, NotificationType.TASK_COMPLETED);
        log.info("Created task completed notification for task {} by user {}", task.getId(), hire.getId());
    }
    
    @Override
    @Transactional
    public void createDocumentSignedNotification(User hr, Task task) {
        String message = String.format("Document for task '%s' has been signed and requires your review", task.getTitle());
        createNotification(hr.getId(), message, task.getId(), NotificationType.DOCUMENT_SIGNED);
        log.info("Created document signed notification for task {} to HR user {}", task.getId(), hr.getId());
    }
    
    @Override
    @Transactional
    public void createReminderNotification(User hire, Task task) {
        String message = String.format("Reminder: Task '%s' is due soon. Please complete it.", task.getTitle());
        createNotification(hire.getId(), message, task.getId(), NotificationType.REMINDER);
        log.info("Created reminder notification for task {} to user {}", task.getId(), hire.getId());
    }
    
    @Override
    @Transactional
    public void createSignatureRequestNotification(User hire, Task task) {
        String message = String.format("Please review and sign the document for task '%s'", task.getTitle());
        createNotification(hire.getId(), message, task.getId(), NotificationType.SIGNATURE_REQUEST);
        log.info("Created signature request notification for task {} to user {}", task.getId(), hire.getId());
    }
    
    @Override
    @Transactional
    public void createOverdueTaskNotification(User hire, Task task) {
        String message = String.format("Task '%s' is overdue. Please complete it as soon as possible.", task.getTitle());
        createNotification(hire.getId(), message, task.getId(), NotificationType.OVERDUE_TASK);
        log.info("Created overdue task notification for task {} to user {}", task.getId(), hire.getId());
    }
    
    @Override
    @Transactional
    public void notifyHRManagersForTask(Task task, String message, NotificationType type) {
        // Find all HR users in the same department or company
        List<HrUser> hrUsers = hrUserRepository.findAll();
        
        for (HrUser hrUser : hrUsers) {
            createNotification(hrUser.getId(), message, task.getId(), type);
        }
        
        log.info("Notified {} HR managers about task {} with type {}", hrUsers.size(), task.getId(), type);
    }
    
    @Override
    @Transactional
    public void createOnboardingStartedNotification(User hire, User hrManager) {
        String message = String.format("Onboarding has started for %s %s", 
            hire.getFirstName(), hire.getLastName());
        createNotification(hrManager.getId(), message, null, NotificationType.ONBOARDING_STARTED);
        log.info("Created onboarding started notification for hire {} to HR {}", hire.getId(), hrManager.getId());
    }
    
    @Override
    @Transactional
    public void createOnboardingCompletedNotification(User hire, User hrManager) {
        String message = String.format("Onboarding has been completed by %s %s", 
            hire.getFirstName(), hire.getLastName());
        createNotification(hrManager.getId(), message, null, NotificationType.ONBOARDING_COMPLETED);
        log.info("Created onboarding completed notification for hire {} to HR {}", hire.getId(), hrManager.getId());
    }
    
    private NotificationDto convertToDto(Notification notification) {
        return convertToDto(notification, determineNotificationType(notification.getMessage()));
    }
    
    private NotificationDto convertToDto(Notification notification, NotificationType type) {
        NotificationDto dto = new NotificationDto();
        dto.setId(notification.getId());
        dto.setUserId(notification.getUser().getId());
        dto.setUserName(notification.getUser().getFirstName() + " " + notification.getUser().getLastName());
        dto.setMessage(notification.getMessage());
        dto.setRead(notification.isRead());
        dto.setCreatedAt(notification.getCreatedAt());
        dto.setNotificationType(type.name());
        
        if (notification.getRelatedTask() != null) {
            dto.setRelatedTaskId(notification.getRelatedTask().getId());
            dto.setRelatedTaskTitle(notification.getRelatedTask().getTitle());
        }
        
        return dto;
    }
    
    private NotificationType determineNotificationType(String message) {
        if (message.contains("completed")) {
            return NotificationType.TASK_COMPLETED;
        } else if (message.contains("signed")) {
            return NotificationType.DOCUMENT_SIGNED;
        } else if (message.contains("Reminder")) {
            return NotificationType.REMINDER;
        } else if (message.contains("overdue")) {
            return NotificationType.OVERDUE_TASK;
        } else if (message.contains("sign the document")) {
            return NotificationType.SIGNATURE_REQUEST;
        } else if (message.contains("Onboarding has started")) {
            return NotificationType.ONBOARDING_STARTED;
        } else if (message.contains("Onboarding has been completed")) {
            return NotificationType.ONBOARDING_COMPLETED;
        } else {
            return NotificationType.GENERAL;
        }
    }
}