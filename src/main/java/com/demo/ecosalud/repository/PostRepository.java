package com.demo.ecosalud.repository;

import com.demo.ecosalud.model.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {

    /** Publicaciones en estado 'published', ordenadas por fecha de publicación descendente. */
    List<Post> findByStatusOrderByPublishedAtDesc(String status);

    /** Busca por slug único (para rutas públicas /publications/:slug). */
    Optional<Post> findBySlug(String slug);

    boolean existsBySlug(String slug);
}
