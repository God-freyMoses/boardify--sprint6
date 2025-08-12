package com.shaper.server.repository;

import com.shaper.server.model.entity.Hire;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface HireRepository extends JpaRepository<Hire, UUID> {
    
    List<Hire> findByDepartment_Id(Integer departmentId);
    
    List<Hire> findByDepartment_Company_Id(Integer companyId);
    
    List<Hire> findByRegisteredByHr_Id(UUID hrId);
    
    @Query("SELECT h FROM Hire h WHERE h.registeredByHr.company.id = :companyId")
    List<Hire> findByCompanyId(@Param("companyId") Integer companyId);
    
    @Query("SELECT COUNT(h) FROM Hire h WHERE h.registeredByHr.id = :hrId")
    long countByHrId(@Param("hrId") UUID hrId);
    
    @Query("SELECT COUNT(h) FROM Hire h WHERE h.department.id = :departmentId")
    long countByDepartmentId(@Param("departmentId") Integer departmentId);
    
    boolean existsByEmailAndRegisteredByHr_Company_Id(String email, Integer companyId);
}
