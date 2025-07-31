package com.shaper.server.model.entity;

import java.util.Set;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    @Column(name = "company_id", updatable = false, nullable = false)
    private Integer id;

    @Column(name = "company_name", nullable = false, unique = true)
    private String name;
    
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<HrUser> hrUsers;
    
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL)
    private Set<CompanyDepartment> departments;
    
    @OneToOne(mappedBy = "company", cascade = CascadeType.ALL)
    private CompanySub subscription;
}