package com.shaper.server.controller;

import com.shaper.server.model.dto.NotificationDto;
import com.shaper.server.model.entity.Task;
import com.shaper.server.model.entity.User;
import com.shaper.server.model.enums.NotificationType;
import com.shaper.server.service.NotificationService;
import com.shaper.server.service.TaskService;
import com.shaper.server.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:4200")
public class NotificationController {
    
    private final NotificationService notificationService;
    private final TaskService taskService;
    private final UserService userService;
    
    @PostMapping
    public ResponseEntity<NotificationDto> createNotification(
            @RequestParam UUID userId,
            @RequestParam String message,
            @RequestParam(required = false) Integer relatedTaskId) {
        try {
            NotificationDto notification = notificationService.createNotification(userId, message, relatedTaskId);
            return ResponseEntity.status(HttpStatus.CREATED).body(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDto>> getNotificationsByUserId(@PathVariable UUID userId) {
        try {
            List<NotificationDto> notifications = notificationService.getNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationDto>> getUnreadNotificationsByUserId(@PathVariable UUID userId) {
        try {
            List<NotificationDto> notifications = notificationService.getUnreadNotificationsByUserId(userId);
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationDto> markAsRead(@PathVariable Integer id) {
        try {
            NotificationDto notification = notificationService.markAsRead(id);
            return ResponseEntity.ok(notification);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Void> markAllAsRead(@PathVariable UUID userId) {
        try {
            notificationService.markAllAsRead(userId);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    @GetMapping("/user/{userId}/unread-count")
    public ResponseEntity<Long> getUnreadCount(@PathVariable UUID userId) {
        try {
            long count = notificationService.getUnreadCount(userId);
            return ResponseEntity.ok(count);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // NEW ENDPOINTS FOR TASK 9
    
    /**
     * HR Management: Send manual reminder to a specific user for a task
     */
    @PostMapping("/reminders/manual")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<NotificationDto> sendManualReminder(
            @RequestParam UUID userId,
            @RequestParam Integer taskId,
            @RequestParam(required = false) String customMessage) {
        try {
            User user = userService.findById(userId);
            Task task = taskService.getTaskById(taskId);
            
            if (customMessage != null && !customMessage.trim().isEmpty()) {
                NotificationDto notification = notificationService.createNotification(
                    userId, customMessage, taskId, NotificationType.REMINDER);
                return ResponseEntity.status(HttpStatus.CREATED).body(notification);
            } else {
                notificationService.createReminderNotification(user, task);
                return ResponseEntity.status(HttpStatus.CREATED).build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * HR Management: Send signature request notification
     */
    @PostMapping("/signature-requests")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> sendSignatureRequest(
            @RequestParam UUID userId,
            @RequestParam Integer taskId) {
        try {
            User user = userService.findById(userId);
            Task task = taskService.getTaskById(taskId);
            notificationService.createSignatureRequestNotification(user, task);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * HR Management: Send overdue task notification
     */
    @PostMapping("/overdue-tasks")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> sendOverdueTaskNotification(
            @RequestParam UUID userId,
            @RequestParam Integer taskId) {
        try {
            User user = userService.findById(userId);
            Task task = taskService.getTaskById(taskId);
            notificationService.createOverdueTaskNotification(user, task);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * HR Management: Bulk send reminders to multiple users for a task
     */
    @PostMapping("/reminders/bulk")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<Void> sendBulkReminders(
            @RequestParam List<UUID> userIds,
            @RequestParam Integer taskId,
            @RequestParam(required = false) String customMessage) {
        try {
            Task task = taskService.getTaskById(taskId);
            
            for (UUID userId : userIds) {
                User user = userService.findById(userId);
                if (customMessage != null && !customMessage.trim().isEmpty()) {
                    notificationService.createNotification(
                        userId, customMessage, taskId, NotificationType.REMINDER);
                } else {
                    notificationService.createReminderNotification(user, task);
                }
            }
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
    
    /**
     * Enhanced notification badge count with filtering
     */
    @GetMapping("/user/{userId}/badge-count")
    public ResponseEntity<NotificationBadgeDto> getNotificationBadgeCount(
            @PathVariable UUID userId,
            @RequestParam(required = false) String type) {
        try {
            long unreadCount = notificationService.getUnreadCount(userId);
            List<NotificationDto> recentNotifications = notificationService.getUnreadNotificationsByUserId(userId)
                .stream()
                .limit(5)
                .toList();
            
            NotificationBadgeDto badge = new NotificationBadgeDto();
            badge.setUnreadCount(unreadCount);
            badge.setRecentNotifications(recentNotifications);
            badge.setHasUrgent(recentNotifications.stream()
                .anyMatch(n -> "OVERDUE_TASK".equals(n.getNotificationType()) || 
                              "SIGNATURE_REQUEST".equals(n.getNotificationType())));
            
            return ResponseEntity.ok(badge);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * HR Dashboard: Get all notifications for HR management
     */
    @GetMapping("/hr/dashboard")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<List<NotificationDto>> getHRDashboardNotifications(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        try {
            // This would typically involve pagination, but for simplicity we'll return recent notifications
            // In a real implementation, you'd want to add pagination support to the service layer
            List<NotificationDto> notifications = notificationService.getNotificationsByUserId(null); // Get all notifications for HR
            return ResponseEntity.ok(notifications);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    // DTO for enhanced badge functionality
    public static class NotificationBadgeDto {
        private long unreadCount;
        private List<NotificationDto> recentNotifications;
        private boolean hasUrgent;
        
        // Getters and setters
        public long getUnreadCount() { return unreadCount; }
        public void setUnreadCount(long unreadCount) { this.unreadCount = unreadCount; }
        
        public List<NotificationDto> getRecentNotifications() { return recentNotifications; }
        public void setRecentNotifications(List<NotificationDto> recentNotifications) { 
            this.recentNotifications = recentNotifications; 
        }
        
        public boolean isHasUrgent() { return hasUrgent; }
        public void setHasUrgent(boolean hasUrgent) { this.hasUrgent = hasUrgent; }
    }
}