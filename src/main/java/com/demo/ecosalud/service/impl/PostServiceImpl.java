package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.PostDTO;
import com.demo.ecosalud.model.entities.Post;
import com.demo.ecosalud.repository.PostRepository;
import com.demo.ecosalud.service.PostService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PostServiceImpl implements PostService {

    private final PostRepository repo;

    @Override
    public List<PostDTO> getAll() {
        return repo.findAll().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<PostDTO> getPublished() {
        return repo.findByStatusOrderByPublishedAtDesc("published").stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public PostDTO getById(Long id) {
        return toDTO(findOrThrow(id));
    }

    @Override
    public PostDTO create(PostDTO dto) {
        Post p = new Post();
        copy(dto, p);
        p.setCreatedAt(LocalDateTime.now());

        // Generar slug único si no se provee
        if (p.getSlug() == null || p.getSlug().isBlank()) {
            p.setSlug(generateUniqueSlug(dto.getTitle()));
        }

        // Si se publica de inmediato, registrar la fecha
        if ("published".equals(p.getStatus()) && p.getPublishedAt() == null) {
            p.setPublishedAt(LocalDateTime.now());
        }

        return toDTO(repo.save(p));
    }

    @Override
    public PostDTO update(Long id, PostDTO dto) {
        Post p = findOrThrow(id);
        String previousStatus = p.getStatus();
        copy(dto, p);
        p.setUpdatedAt(LocalDateTime.now());

        // Registrar publishedAt la primera vez que pasa a published
        if ("published".equals(p.getStatus()) && !"published".equals(previousStatus)
                && p.getPublishedAt() == null) {
            p.setPublishedAt(LocalDateTime.now());
        }

        return toDTO(repo.save(p));
    }

    @Override
    public void delete(Long id) {
        if (!repo.existsById(id)) throw new ResourceNotFoundException("Post no encontrado: " + id);
        repo.deleteById(id);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Post findOrThrow(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Post no encontrado: " + id));
    }

    private void copy(PostDTO dto, Post p) {
        if (dto.getTitle()      != null) p.setTitle(dto.getTitle());
        if (dto.getSlug()       != null) p.setSlug(dto.getSlug());
        if (dto.getExcerpt()    != null) p.setExcerpt(dto.getExcerpt());
        if (dto.getContent()    != null) p.setContent(dto.getContent());
        if (dto.getImageUrl()   != null) p.setImageUrl(dto.getImageUrl());
        if (dto.getStatus()     != null) p.setStatus(dto.getStatus());
        if (dto.getTags()       != null) p.setTags(dto.getTags());
        if (dto.getAuthorId()   != null) p.setAuthorId(dto.getAuthorId());
        if (dto.getAuthorName() != null) p.setAuthorName(dto.getAuthorName());
        if (dto.getPublishedAt()!= null) p.setPublishedAt(dto.getPublishedAt());
    }

    private PostDTO toDTO(Post p) {
        PostDTO dto = new PostDTO();
        dto.setId(p.getId());
        dto.setTitle(p.getTitle());
        dto.setSlug(p.getSlug());
        dto.setExcerpt(p.getExcerpt());
        dto.setContent(p.getContent());
        dto.setImageUrl(p.getImageUrl());
        dto.setStatus(p.getStatus());
        dto.setTags(p.getTags());
        dto.setAuthorId(p.getAuthorId());
        dto.setAuthorName(p.getAuthorName());
        dto.setPublishedAt(p.getPublishedAt());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());
        return dto;
    }

    /**
     * Genera un slug URL-friendly único a partir del título.
     * Ej: "Terapia Cráneo-Sacral 101" → "terapia-craneo-sacral-101"
     * Si el slug ya existe, añade un sufijo numérico.
     */
    private String generateUniqueSlug(String title) {
        String base = Normalizer.normalize(title == null ? "post" : title, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("[\\s-]+", "-");

        if (base.isBlank()) base = "post";
        if (base.length() > 200) base = base.substring(0, 200);

        String slug = base;
        int i = 1;
        while (repo.existsBySlug(slug)) {
            slug = base + "-" + i++;
        }
        return slug;
    }
}
