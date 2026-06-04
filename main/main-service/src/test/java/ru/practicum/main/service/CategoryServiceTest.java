package ru.practicum.main.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private EventRepository eventRepository;

    @InjectMocks
    private CategoryService categoryService;

    @Test
    void create_savesCategory() {
        NewCategoryDto dto = NewCategoryDto.builder().name("Music").build();
        Category saved = Category.builder().id(1L).name("Music").build();
        when(categoryRepository.save(org.mockito.ArgumentMatchers.any())).thenReturn(saved);

        assertEquals("Music", categoryService.create(dto).getName());
    }

    @Test
    void delete_whenCategoryHasEvents_throwsConflict() {
        Category category = Category.builder().id(1L).name("Music").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.countByCategoryId(1L)).thenReturn(2L);

        assertThrows(ConflictException.class, () -> categoryService.delete(1L));
    }

    @Test
    void delete_whenEmpty_deletesCategory() {
        Category category = Category.builder().id(1L).name("Music").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(eventRepository.countByCategoryId(1L)).thenReturn(0L);

        categoryService.delete(1L);
        verify(categoryRepository).delete(category);
    }

    @Test
    void getById_whenMissing_throwsNotFound() {
        when(categoryRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> categoryService.getById(99L));
    }

    @Test
    void getAll_returnsAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                Category.builder().id(1L).name("A").build()));
        assertEquals(1, categoryService.getAll().size());
    }

    @Test
    void update_changesName() {
        Category category = Category.builder().id(1L).name("Old").build();
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));
        when(categoryRepository.save(category)).thenReturn(category);

        CategoryDto result = categoryService.update(1L, CategoryDto.builder().id(1L).name("New").build());
        assertEquals("New", result.getName());
    }
}
