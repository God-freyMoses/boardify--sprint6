package com.shaper.server.model.entity;

import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "departments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CompanyDepartment {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "department_id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "department_name", nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne
    @JoinColumn(name = "hr_manager_id", nullable = false)
    private HrUser createdByHr;

    @OneToMany(mappedBy = "department", cascade = CascadeType.ALL)
    private Set<Hire> hires;
    
    @ManyToMany
    @JoinTable(
        name = "department_templates",
        joinColumns = @JoinColumn(name = "department_id"),
        inverseJoinColumns = @JoinColumn(name = "template_id")
    )
    private Set<Template> assignedTemplates;
}

