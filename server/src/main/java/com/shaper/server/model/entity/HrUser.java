
package com.shaper.server.model.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.shaper.server.model.enums.UserRole;

@Entity
@Table(name = "HR_Manager")
@DiscriminatorValue("HR")
@Getter
@Setter
@NoArgsConstructor
public class HrUser extends User {

    @ManyToOne
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @OneToMany(mappedBy = "createdByHr", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CompanyDepartment> departments;

    @OneToMany(mappedBy = "registeredByHr", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Hire> hires;
    
    @OneToMany(mappedBy = "createdByHr", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Template> templates;
    
    @PrePersist
    protected void onHrCreate() {
        setRole(UserRole.HR_MANAGER);
    }
}
