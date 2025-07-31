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
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Task.TaskStatus status;
    
    @Column(name = "completed_at")
    private LocalDateTime completedAt;
    
    @Column(name = "signed_at")
    private LocalDateTime signedAt;
    
    @Column(name = "signature_data")
    private String signatureData;
    
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = Task.TaskStatus.PENDING;
    }
}