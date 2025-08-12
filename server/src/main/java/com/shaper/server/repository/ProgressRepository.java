package com.shaper.server.repository;

import com.shaper.server.model.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Integer> {
    
    List<Progress> findByHire_Id(UUID hireId);
    
    List<Progress> findByTemplate_Id(Integer templateId);
    
    @Query("SELECT p FROM Progress p WHERE p.hire.id = :hireId AND p.template.id = :templateId")
    Optional<Progress> findByHireIdAndTemplateId(@Param("hireId") UUID hireId, @Param("templateId") Integer templateId);
    
    @Query("SELECT p FROM Progress p WHERE p.hire.registeredByHr.id = :hrId")
    List<Progress> findByHrId(@Param("hrId") UUID hrId);
    
    @Query("SELECT p FROM Progress p WHERE p.hire.department.company.id = :companyId")
    List<Progress> findByCompanyId(@Param("companyId") Integer companyId);
    
    @Query("SELECT p FROM Progress p WHERE p.hire.department.id = :departmentId")
    List<Progress> findByDepartmentId(@Param("departmentId") Integer departmentId);
    
    @Query("SELECT AVG(p.completionPercentage) FROM Progress p WHERE p.hire.registeredByHr.id = :hrId")
    Double getAverageCompletionByHrId(@Param("hrId") UUID hrId);
    
    @Query("SELECT COUNT(p) FROM Progress p WHERE p.hire.registeredByHr.id = :hrId AND p.completionPercentage = 100.0")
    long countCompletedByHrId(@Param("hrId") UUID hrId);
}