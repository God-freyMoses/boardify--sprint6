package com.shaper.server.repository;
import com.shaper.server.model.entity.HrUser;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

public interface  HrUserRepository extends JpaRepository<HrUser, UUID> {

    HrUser findByCompanyId(Integer companyId);
    
    // Add other methods as necessary
    
}
