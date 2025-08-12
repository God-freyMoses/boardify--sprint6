package com.shaper.server.repository;

import com.shaper.server.model.entity.HrUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface HrUserRepository extends JpaRepository<HrUser, UUID> {

    List<HrUser> findByCompany_Id(Integer companyId);
    
    Optional<HrUser> findByEmail(String email);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT hr FROM HrUser hr WHERE hr.company.id = :companyId AND hr.email = :email")
    Optional<HrUser> findByCompanyIdAndEmail(@Param("companyId") Integer companyId, @Param("email") String email);
    
    @Query("SELECT COUNT(hr) FROM HrUser hr WHERE hr.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Integer companyId);
}
