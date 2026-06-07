package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.PlatformPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlatformPostRepository extends JpaRepository<PlatformPost, Long> {

    /** Retorna los posts publicados ordenados del más reciente al más antiguo. */
    List<PlatformPost> findByStatusOrderByPublishedAtDesc(String status);

    /** Todos los posts (para el panel super-admin), más reciente primero. */
    List<PlatformPost> findAllByOrderByCreatedAtDesc();
}
