package com.demo.ecosalud.controller;

import com.demo.ecosalud.model.dto.PlatformPostDTO;
import com.demo.ecosalud.model.entities.PlatformPost;
import com.demo.ecosalud.repository.PlatformPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Blog de la plataforma Ecosalud Market.
 *
 * <p>Endpoints públicos (lectura) en {@code /api/public/blog} — accesibles sin JWT.<br>
 * Endpoints de gestión en {@code /api/platform/blog} — solo super-admin (JWT sin tenant claim).</p>
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class PlatformBlogController {

    private final PlatformPostRepository repo;

    // ═══════════════════════════════════════════════════════
    // PÚBLICO — lectura sin JWT
    // ═══════════════════════════════════════════════════════

    /** Lista de posts publicados (sin contenido completo para cargar rápido). */
    @GetMapping("/api/public/blog")
    public List<PlatformPostDTO> listPublished() {
        return repo.findByStatusOrderByPublishedAtDesc("PUBLISHED")
                .stream()
                .map(p -> toDto(p, false))
                .collect(Collectors.toList());
    }

    /** Detalle completo de un post publicado. */
    @GetMapping("/api/public/blog/{id}")
    public ResponseEntity<PlatformPostDTO> getPublished(@PathVariable Long id) {
        return repo.findById(id)
                .filter(p -> "PUBLISHED".equals(p.getStatus()))
                .map(p -> ResponseEntity.ok(toDto(p, true)))
                .orElse(ResponseEntity.notFound().build());
    }

    // ═══════════════════════════════════════════════════════
    // SUPER-ADMIN — CRUD (requiere JWT sin tenant claim)
    // ═══════════════════════════════════════════════════════

    /** Lista todos los posts (borradores + publicados). */
    @GetMapping("/api/platform/blog")
    public List<PlatformPostDTO> listAll() {
        return repo.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(p -> toDto(p, false))
                .collect(Collectors.toList());
    }

    /** Crea un nuevo post (status=DRAFT por defecto). */
    @PostMapping("/api/platform/blog")
    public ResponseEntity<PlatformPostDTO> create(@RequestBody PlatformPostDTO dto) {
        PlatformPost post = PlatformPost.builder()
                .title(dto.getTitle())
                .excerpt(dto.getExcerpt())
                .content(dto.getContent())
                .imageUrl(dto.getImageUrl())
                .tags(dto.getTagsRaw())
                .category(dto.getCategory() != null ? dto.getCategory() : "GENERAL")
                .status("DRAFT")
                .authorName(dto.getAuthorName() != null ? dto.getAuthorName() : "Equipo Ecosalud")
                .build();
        PlatformPost saved = repo.save(post);
        log.info("[PlatformBlog] Post creado: id={}, title='{}'", saved.getId(), saved.getTitle());
        return ResponseEntity.ok(toDto(saved, true));
    }

    /** Actualiza un post existente. */
    @PutMapping("/api/platform/blog/{id}")
    public ResponseEntity<PlatformPostDTO> update(@PathVariable Long id,
                                                   @RequestBody PlatformPostDTO dto) {
        return repo.findById(id).map(post -> {
            if (dto.getTitle()      != null) post.setTitle(dto.getTitle());
            if (dto.getExcerpt()    != null) post.setExcerpt(dto.getExcerpt());
            if (dto.getContent()    != null) post.setContent(dto.getContent());
            if (dto.getImageUrl()   != null) post.setImageUrl(dto.getImageUrl());
            if (dto.getTagsRaw()    != null) post.setTags(dto.getTagsRaw());
            if (dto.getCategory()   != null) post.setCategory(dto.getCategory());
            if (dto.getAuthorName() != null) post.setAuthorName(dto.getAuthorName());
            return ResponseEntity.ok(toDto(repo.save(post), true));
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Publica o despublica un post.
     * Si se publica y no tenía publishedAt, se asigna la fecha actual.
     */
    @PatchMapping("/api/platform/blog/{id}/publish")
    public ResponseEntity<PlatformPostDTO> togglePublish(@PathVariable Long id) {
        return repo.findById(id).map(post -> {
            boolean nowPublished = !"PUBLISHED".equals(post.getStatus());
            post.setStatus(nowPublished ? "PUBLISHED" : "DRAFT");
            if (nowPublished && post.getPublishedAt() == null) {
                post.setPublishedAt(LocalDateTime.now());
            }
            log.info("[PlatformBlog] Post id={} → status={}", id, post.getStatus());
            return ResponseEntity.ok(toDto(repo.save(post), false));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Elimina un post. */
    @DeleteMapping("/api/platform/blog/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!repo.existsById(id)) return ResponseEntity.notFound().build();
        repo.deleteById(id);
        log.info("[PlatformBlog] Post id={} eliminado", id);
        return ResponseEntity.noContent().build();
    }

    // ═══════════════════════════════════════════════════════
    // Helpers
    // ═══════════════════════════════════════════════════════

    private PlatformPostDTO toDto(PlatformPost p, boolean includeContent) {
        List<String> tags = (p.getTags() != null && !p.getTags().isBlank())
                ? Arrays.asList(p.getTags().split(","))
                : List.of();

        return PlatformPostDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .excerpt(p.getExcerpt())
                .content(includeContent ? p.getContent() : null)
                .imageUrl(p.getImageUrl())
                .tags(tags)
                .tagsRaw(p.getTags())
                .category(p.getCategory())
                .status(p.getStatus())
                .authorName(p.getAuthorName())
                .publishedAt(p.getPublishedAt())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
