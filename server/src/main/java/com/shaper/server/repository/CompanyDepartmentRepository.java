package com.shaper.server.repository;
import com.shaper.server.model.entity.CompanyDepartment;
import org.springframework.data.jpa.repository.JpaRepository;


public interface CompanyDepartmentRepository  extends JpaRepository<CompanyDepartment, Integer> {
    
   
    CompanyDepartment findByCompany_Id(Integer companyId);
    CompanyDepartment findByName(String name);
    boolean existsByName(String name);
}
