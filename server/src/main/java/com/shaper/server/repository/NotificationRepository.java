package com.shaper.server.repository;

import com.shaper.server.model.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    
    List<Notification> findByUser_IdOrderByCreatedAtDesc(UUID userId);
    
    List<Notification> findByUser_IdAndIsReadOrderByCreatedAtDesc(UUID userId, boolean isRead);
    
    List<Notification> findByRelatedTask_Id(Integer taskId);
    
    long countByUser_IdAndIsRead(UUID userId, boolean isRead);
}