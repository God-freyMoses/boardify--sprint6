package com.shaper.server.repository;

import com.shaper.server.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {

    User findByEmail(String email);
    boolean existsByEmail(String email);
    
    // For Spring Security compatibility
    default User findByUsername(String username) {
        return findByEmail(username);
    }
    
    default boolean existsByUsername(String username) {
        return existsByEmail(username);
    }

}
