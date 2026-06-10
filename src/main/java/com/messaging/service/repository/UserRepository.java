package com.messaging.service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;
import com.messaging.service.domain.MyUser;

public interface UserRepository extends JpaRepository<MyUser, UUID> {
    Optional<MyUser> findByUsername(String username);
    boolean existsByUsername(String username);
}