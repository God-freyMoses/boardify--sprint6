package com.shaper.server.model.entity;

import java.time.LocalDateTime;
import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "tasks")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "task_id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TaskStatus status;
    
    @Column(name = "requires_signature", nullable = false)
    private boolean requiresSignature;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private Set<Document> documents;
    
    @ManyToMany(mappedBy = "tasks")
    private Set<Template> templates;
    
    @OneToMany(mappedBy = "task")
    private Set<Progress> progressItems;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = TaskStatus.PENDING;
    }

    public enum TaskStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }
}
