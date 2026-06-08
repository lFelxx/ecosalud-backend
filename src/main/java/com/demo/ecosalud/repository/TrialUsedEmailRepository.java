package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.TrialUsedEmail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TrialUsedEmailRepository extends JpaRepository<TrialUsedEmail, Long> {

    boolean existsByEmail(String email);
}
