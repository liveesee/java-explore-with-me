package ru.practicum.main.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.util.PageUtil;
import ru.practicum.main.dto.CategoryDto;
import ru.practicum.main.dto.NewCategoryDto;
import ru.practicum.main.exception.ConflictException;
import ru.practicum.main.exception.NotFoundException;
import ru.practicum.main.mapper.CategoryMapper;
import ru.practicum.main.model.Category;
import ru.practicum.main.repository.CategoryRepository;
import ru.practicum.main.repository.EventRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        return CategoryMapper.toDto(categoryRepository.save(CategoryMapper.toEntity(dto)));
    }

    @Transactional
    public CategoryDto update(Long catId, CategoryDto dto) {
        Category category = getCategoryOrThrow(catId);
        category.setName(dto.getName());
        return CategoryMapper.toDto(categoryRepository.save(category));
    }

    @Transactional
    public void delete(Long catId) {
        Category category = getCategoryOrThrow(catId);
        if (eventRepository.countByCategoryId(catId) > 0) {
            throw new ConflictException("The category is not empty");
        }
        categoryRepository.delete(category);
    }

    public List<CategoryDto> getAll(int from, int size) {
        Pageable pageable = PageUtil.createPageable(from, size);
        return categoryRepository.findAll(pageable).stream().map(CategoryMapper::toDto).toList();
    }

    public CategoryDto getById(Long catId) {
        return CategoryMapper.toDto(getCategoryOrThrow(catId));
    }

    public Category getCategoryOrThrow(Long catId) {
        return categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category with id=" + catId + " was not found"));
    }
}
