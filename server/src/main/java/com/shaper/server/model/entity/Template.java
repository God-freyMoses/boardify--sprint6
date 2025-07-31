package com.shaper.server.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(name = "templates")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Template {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "template_id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "description")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private TemplateStatus status;
    
    @ManyToOne
    @JoinColumn(name = "hr_id", nullable = false)
    private HrUser createdByHr;
    
    @ManyToMany
    @JoinTable(
        name = "template_tasks",
        joinColumns = @JoinColumn(name = "template_id"),
        inverseJoinColumns = @JoinColumn(name = "task_id")
    )
    private Set<Task> tasks;
    
    @ManyToMany(mappedBy = "assignedTemplates")
    private Set<CompanyDepartment> departments;

    @Column(name = "created_date", nullable = false, updatable = false)
    private LocalDateTime createdDate;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;
    
    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
        updatedDate = LocalDateTime.now();
        status = TemplateStatus.PENDING;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public enum TemplateStatus {
        PENDING,
        IN_PROGRESS,
        COMPLETED
    }
}
