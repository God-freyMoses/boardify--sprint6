package com.shaper.server.repository;

import com.shaper.server.model.entity.CompanyDepartment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyDepartmentRepository extends JpaRepository<CompanyDepartment, Integer> {
    
    List<CompanyDepartment> findByCompany_Id(Integer companyId);
    
    List<CompanyDepartment> findByCreatedByHr_Id(UUID hrId);
    
    Optional<CompanyDepartment> findByNameAndCompany_Id(String name, Integer companyId);
    
    boolean existsByNameAndCompany_Id(String name, Integer companyId);
    
    @Query("SELECT d FROM CompanyDepartment d JOIN d.assignedTemplates t WHERE t.id = :templateId")
    List<CompanyDepartment> findByTemplateId(@Param("templateId") Integer templateId);
    
    @Query("SELECT COUNT(d) FROM CompanyDepartment d WHERE d.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);
}
