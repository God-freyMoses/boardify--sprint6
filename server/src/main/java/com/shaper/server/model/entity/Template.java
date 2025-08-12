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
    private com.shaper.server.model.enums.TemplateStatus status;
    
    @ManyToOne
    @JoinColumn(name = "hr_id", nullable = false)
    private HrUser createdByHr;
    
    @OneToMany(mappedBy = "template", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
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
        status = com.shaper.server.model.enums.TemplateStatus.PENDING;
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedDate = LocalDateTime.now();
    }

    public void setHrUser(HrUser testHrUser) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'setHrUser'");
    }

    public CompanyDepartment getCompany() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getCompany'");
    }


}
