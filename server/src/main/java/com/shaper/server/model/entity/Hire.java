package com.shaper.server.model.entity;

import java.util.Set;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import com.shaper.server.model.enums.UserRole;

@Entity
@Table(name = "Hire_User")
@DiscriminatorValue("HIRE")
@Getter
@Setter
@NoArgsConstructor
public class Hire extends User {

    @Column(name = "gender")
    private String gender;
    
    @Column(name = "title")
    private String title;
    
    @Column(name = "picture_url")
    private String pictureUrl;

    @ManyToOne
    @JoinColumn(name = "hr_id", nullable = false)
    private HrUser registeredByHr;
    
    @ManyToOne
    @JoinColumn(name = "department_id")
    private CompanyDepartment department;
    
    @OneToMany(mappedBy = "hire", cascade = CascadeType.ALL)
    private Set<Progress> progressItems;
    
    @PrePersist
    protected void onHireCreate() {
        setRole(UserRole.NEW_HIRE);
    }
}
