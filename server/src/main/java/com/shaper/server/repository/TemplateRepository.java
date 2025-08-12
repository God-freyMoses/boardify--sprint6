package com.shaper.server.repository;

import com.shaper.server.model.entity.Template;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateRepository extends JpaRepository<Template, Integer> {
    
    @Query("SELECT t FROM Template t JOIN t.departments d WHERE d.id = :departmentId")
    List<Template> findByDepartmentId(@Param("departmentId") Integer departmentId);
    
    List<Template> findByCreatedByHr_Id(java.util.UUID hrId);
    
    List<Template> findByStatus(com.shaper.server.model.enums.TemplateStatus status);
    
    @Query("SELECT t FROM Template t JOIN t.departments d WHERE d.company.id = :companyId")
    List<Template> findByCompanyId(@Param("companyId") Integer companyId);
}