package com.shaper.server.model.entity;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "progress")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Progress {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "progress_id", updatable = false, nullable = false)
    private Integer id;
    
    @ManyToOne
    @JoinColumn(name = "hire_id", nullable = false)
    private Hire hire;
    
    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    @Column(name = "total_tasks", nullable = false)
    private Integer totalTasks;
    
    @Column(name = "completed_tasks", nullable = false)
    private Integer completedTasks;
    
    @Column(name = "completion_percentage", nullable = false)
    private Double completionPercentage;
    
    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        lastUpdated = LocalDateTime.now();
        if (completedTasks == null) completedTasks = 0;
        if (totalTasks == null) totalTasks = 0;
        if (completionPercentage == null) completionPercentage = 0.0;
    }
    
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
        if (totalTasks > 0) {
            completionPercentage = (completedTasks.doubleValue() / totalTasks.doubleValue()) * 100.0;
        } else {
            completionPercentage = 0.0;
        }
    }
}