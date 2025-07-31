package com.shaper.server.repository;
import com.shaper.server.model.entity.Hire;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HireRepository  extends JpaRepository<Hire, UUID> {
    // Define custom query methods if needed
Hire findByDepartmentId(Integer departmentId);
    Hire findByDepartment_Company_Id(Integer companyId);
  
    
    // Add other methods as necessary
    
}
