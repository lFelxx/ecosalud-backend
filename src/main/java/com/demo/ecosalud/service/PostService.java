package com.demo.ecosalud.service;

import com.demo.ecosalud.model.dto.PostDTO;

import java.util.List;

/** Contrato del servicio de publicaciones del blog de la clínica. */
public interface PostService {

    List<PostDTO> getAll();

    /** Solo publicaciones en estado 'published' (para el sitio público). */
    List<PostDTO> getPublished();

    PostDTO getById(Long id);

    PostDTO create(PostDTO dto);

    PostDTO update(Long id, PostDTO dto);

    void delete(Long id);
}
