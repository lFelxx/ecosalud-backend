package com.demo.ecosalud.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.demo.ecosalud.enums.UserStatus;
import com.demo.ecosalud.model.entities.User;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Boolean existsByEmail(String email);

    Boolean existsByName(String name);

    Boolean existsByStatus(UserStatus status);
}
