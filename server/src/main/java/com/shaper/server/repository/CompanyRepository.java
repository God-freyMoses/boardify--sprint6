package com.shaper.server.repository;

import com.shaper.server.model.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Integer> {
    Company findByName(String name);
    boolean existsByName(String name);
}