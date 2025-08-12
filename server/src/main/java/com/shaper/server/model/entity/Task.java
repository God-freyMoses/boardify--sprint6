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
    private com.shaper.server.model.enums.TaskStatus status;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "task_type", nullable = false)
    private com.shaper.server.model.enums.TaskType taskType;
    
    @Column(name = "requires_signature", nullable = false)
    private boolean requiresSignature;
    
    @Column(name = "resource_url")
    private String resourceUrl;
    
    @Column(name = "event_date")
    private LocalDateTime eventDate;
    
    @Column(name = "priority")
    @Enumerated(EnumType.STRING)
    private com.shaper.server.model.enums.TaskPriority priority;
    
    @Column(name = "estimated_hours")
    private Double estimatedHours;
    
    @Column(name = "order_index")
    private Integer orderIndex;

    @ManyToOne
    @JoinColumn(name = "template_id", nullable = false)
    private Template template;
    
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL)
    private Set<Document> documents;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        status = com.shaper.server.model.enums.TaskStatus.PENDING;
    }


}
