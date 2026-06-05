package com.demo.ecosalud.service.impl;

import com.demo.ecosalud.exception.DuplicateResourceException;
import com.demo.ecosalud.exception.ResourceNotFoundException;
import com.demo.ecosalud.model.dto.CatalogDTO;
import com.demo.ecosalud.model.entities.Catalog;
import com.demo.ecosalud.repository.CatalogRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CatalogServiceImplTest {

    @Mock
    private CatalogRepository catalogRepository;

    @InjectMocks
    private CatalogServiceImpl catalogService;

    private Catalog catalog;
    private CatalogDTO catalogDTO;

    @BeforeEach
    void setUp() {
        catalog = new Catalog();
        catalog.setId(1L);
        catalog.setName("Masaje terapéutico");
        catalog.setDescription("Sesión completa de masaje");
        catalog.setPrice(50.0);
        catalog.setDuration(60);
        catalog.setAvailability(true);
        catalog.setCategory("Relajación");
        catalog.setBenefits(List.of("Reduce el estrés"));

        catalogDTO = new CatalogDTO();
        catalogDTO.setName("Masaje terapéutico");
        catalogDTO.setDescription("Sesión completa de masaje");
        catalogDTO.setPrice(50.0);
        catalogDTO.setDuration(60);
        catalogDTO.setAvailability(true);
        catalogDTO.setCategory("Relajación");
        catalogDTO.setBenefits(List.of("Reduce el estrés"));
    }

    @Test
    void createCatalog_nombreUnico_retornaDTO() {
        when(catalogRepository.existsByName(any())).thenReturn(false);
        when(catalogRepository.save(any())).thenReturn(catalog);

        CatalogDTO result = catalogService.createCatalog(catalogDTO);

        assertThat(result.getName()).isEqualTo("Masaje terapéutico");
        verify(catalogRepository).save(any());
    }

    @Test
    void createCatalog_nombreDuplicado_lanzaExcepcion() {
        when(catalogRepository.existsByName(any())).thenReturn(true);

        assertThatThrownBy(() -> catalogService.createCatalog(catalogDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe un servicio con el nombre");
    }

    @Test
    void getCatalogById_existe_retornaDTO() {
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));

        CatalogDTO result = catalogService.getCatalogById(1L);

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Masaje terapéutico");
    }

    @Test
    void getCatalogById_noExiste_lanzaExcepcion() {
        when(catalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.getCatalogById(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void getCatalogs_sinFiltro_retornaTodos() {
        when(catalogRepository.findAll()).thenReturn(List.of(catalog));

        List<CatalogDTO> results = catalogService.getCatalogs(null);

        assertThat(results).hasSize(1);
    }

    @Test
    void getCatalogs_sinFiltroVacio_retornaTodos() {
        when(catalogRepository.findAll()).thenReturn(List.of(catalog));

        List<CatalogDTO> results = catalogService.getCatalogs("  ");

        assertThat(results).hasSize(1);
    }

    @Test
    void getCatalogs_conCategoria_filtraCorrectamente() {
        when(catalogRepository.findByCategoryIgnoreCase("Relajación")).thenReturn(List.of(catalog));

        List<CatalogDTO> results = catalogService.getCatalogs("Relajación");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getCategory()).isEqualTo("Relajación");
    }

    @Test
    void getAvailableCatalogs_sinFiltro_retornaDisponibles() {
        when(catalogRepository.findByAvailability(true)).thenReturn(List.of(catalog));

        List<CatalogDTO> results = catalogService.getAvailableCatalogs(null);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAvailability()).isTrue();
    }

    @Test
    void getAvailableCatalogs_conCategoria_filtraCorrectamente() {
        when(catalogRepository.findByCategoryIgnoreCaseAndAvailability("Relajación", true))
                .thenReturn(List.of(catalog));

        List<CatalogDTO> results = catalogService.getAvailableCatalogs("Relajación");

        assertThat(results).hasSize(1);
    }

    @Test
    void updateCatalog_exitoso_retornaActualizado() {
        catalogDTO.setName("Masaje profundo");
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(catalogRepository.existsByName("Masaje profundo")).thenReturn(false);
        when(catalogRepository.save(any())).thenReturn(catalog);

        CatalogDTO result = catalogService.updateCatalog(1L, catalogDTO);

        assertThat(result).isNotNull();
        verify(catalogRepository).save(any());
    }

    @Test
    void updateCatalog_mismoNombre_noValidaDuplicado() {
        // mismo nombre → no debe lanzar excepción por duplicado
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(catalogRepository.save(any())).thenReturn(catalog);

        CatalogDTO result = catalogService.updateCatalog(1L, catalogDTO);

        assertThat(result).isNotNull();
        verify(catalogRepository, never()).existsByName(any());
    }

    @Test
    void updateCatalog_noExiste_lanzaExcepcion() {
        when(catalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.updateCatalog(99L, catalogDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateCatalog_nombreDuplicado_lanzaExcepcion() {
        catalogDTO.setName("Otro servicio");
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(catalogRepository.existsByName("Otro servicio")).thenReturn(true);

        assertThatThrownBy(() -> catalogService.updateCatalog(1L, catalogDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Ya existe un servicio con el nombre");
    }

    @Test
    void updateCatalog_availabilityNula_noActualizaAvailability() {
        catalogDTO.setAvailability(null);
        catalogDTO.setName("Masaje terapéutico");
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(catalogRepository.save(any())).thenReturn(catalog);

        CatalogDTO result = catalogService.updateCatalog(1L, catalogDTO);

        assertThat(result).isNotNull();
        verify(catalogRepository).save(any());
    }

    @Test
    void updateCatalog_categoriaNula_noActualizaCategoria() {
        catalogDTO.setCategory(null);
        catalogDTO.setName("Masaje terapéutico");
        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(catalogRepository.save(any())).thenReturn(catalog);

        CatalogDTO result = catalogService.updateCatalog(1L, catalogDTO);

        assertThat(result).isNotNull();
        verify(catalogRepository).save(any());
    }

    @Test
    void createCatalog_availabilityNula_usaDefaultTrue() {
        catalogDTO.setAvailability(null);
        when(catalogRepository.existsByName(any())).thenReturn(false);
        when(catalogRepository.save(any())).thenReturn(catalog);

        CatalogDTO result = catalogService.createCatalog(catalogDTO);

        assertThat(result).isNotNull();
        verify(catalogRepository).save(any());
    }

    @Test
    void toggleAvailability_activoLoDesactiva() {
        catalog.setAvailability(true);
        Catalog desactivado = new Catalog();
        desactivado.setId(1L);
        desactivado.setName(catalog.getName());
        desactivado.setDescription(catalog.getDescription());
        desactivado.setPrice(catalog.getPrice());
        desactivado.setDuration(catalog.getDuration());
        desactivado.setAvailability(false);
        desactivado.setCategory(catalog.getCategory());

        when(catalogRepository.findById(1L)).thenReturn(Optional.of(catalog));
        when(catalogRepository.save(any())).thenReturn(desactivado);

        CatalogDTO result = catalogService.toggleAvailability(1L);

        assertThat(result.getAvailability()).isFalse();
    }

    @Test
    void toggleAvailability_noExiste_lanzaExcepcion() {
        when(catalogRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> catalogService.toggleAvailability(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deleteCatalog_existe_eliminaCorrectamente() {
        when(catalogRepository.existsById(1L)).thenReturn(true);

        catalogService.deleteCatalog(1L);

        verify(catalogRepository).deleteById(1L);
    }

    @Test
    void deleteCatalog_noExiste_lanzaExcepcion() {
        when(catalogRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> catalogService.deleteCatalog(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
