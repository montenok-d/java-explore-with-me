package ru.practicum.ewm.category.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ewm.category.dto.CategoryDto;
import ru.practicum.ewm.category.dto.NewCategoryDto;
import ru.practicum.ewm.category.mapper.CategoryMapper;
import ru.practicum.ewm.category.model.Category;
import ru.practicum.ewm.category.repository.CategoryRepository;
import ru.practicum.ewm.event.repository.EventRepository;
import ru.practicum.ewm.exception.NotFoundException;
import ru.practicum.ewm.exception.ValidationException;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;


    @Transactional
    @Override
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        if (categoryRepository.existsByName(newCategoryDto.getName())) {
            throw new ValidationException("Category with this name already exists.");
        }
        Category category = categoryRepository.save(CategoryMapper.newCategoryToCategory(newCategoryDto));
        return CategoryMapper.toCategoryDto(category);
    }

    @Transactional
    @Override
    public void deleteCategory(long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category id: {}" + catId + "not found."));
        if (eventRepository.existsByCategoryId(catId)) {
            throw new ValidationException("Cannot delete category with existing events.");
        }
        categoryRepository.deleteById(catId);
    }

    @Transactional
    @Override
    public CategoryDto updateCategory(long catId, CategoryDto categoryDto) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category id: " + catId + "not found."));
        Category categoryByName = categoryRepository.findByName(categoryDto.getName());
        if (categoryByName != null) {
            if (catId != categoryByName.getId()) {
                throw new ValidationException("Category with this name already exists.");
            }
        }
        categoryDto.setId(catId);
        Category categorySaved = categoryRepository.save(CategoryMapper.dtoToCategory(categoryDto));
        return CategoryMapper.toCategoryDto(categorySaved);
    }

    @Override
    public List<CategoryDto> findAllCategories(int from, int size) {
        List<Category> categories = new ArrayList<>();
        PageRequest pageable = PageRequest.of(from, size);
        categories = categoryRepository.findAll(pageable).getContent();
        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto findCategory(long catId) {
        Category category = categoryRepository.findById(catId)
                .orElseThrow(() -> new NotFoundException("Category id: " + catId + "not found."));
        return CategoryMapper.toCategoryDto(category);
    }
}
