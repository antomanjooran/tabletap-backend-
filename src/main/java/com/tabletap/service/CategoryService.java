package com.tabletap.service;

import com.tabletap.dto.request.CategoryRequest;
import com.tabletap.dto.request.ReorderRequest;
import com.tabletap.dto.response.CategorySimpleResponse;
import com.tabletap.entity.Category;
import com.tabletap.exception.ApiException;
import com.tabletap.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<CategorySimpleResponse> getCategories() {
        return categoryRepository.findAll().stream()
                .filter(Category::getIsActive)
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .map(c -> new CategorySimpleResponse(c.getId(), c.getName(), c.getSortOrder()))
                .toList();
    }

    @Transactional
    public CategorySimpleResponse createCategory(CategoryRequest req) {
        Category cat = Category.builder()
                .name(req.name())
                .sortOrder(req.sortOrder() != null ? req.sortOrder() : 999)
                .isActive(true)
                .build();
        cat = categoryRepository.save(cat);
        return new CategorySimpleResponse(cat.getId(), cat.getName(), cat.getSortOrder());
    }

    @Transactional
    public CategorySimpleResponse updateCategory(UUID id, Map<String, Object> updates) {
        Category cat = categoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Category not found", "CAT_NOT_FOUND", 404));
        if (updates.containsKey("name")) cat.setName((String) updates.get("name"));
        if (updates.containsKey("sortOrder")) cat.setSortOrder(((Number) updates.get("sortOrder")).intValue());
        cat = categoryRepository.save(cat);
        return new CategorySimpleResponse(cat.getId(), cat.getName(), cat.getSortOrder());
    }

    @Transactional
    public void reorder(List<ReorderRequest> updates) {
        updates.forEach(u -> categoryRepository.findById(u.id()).ifPresent(cat -> {
            cat.setSortOrder(u.sortOrder());
            categoryRepository.save(cat);
        }));
    }

    @Transactional
    public void deleteCategory(UUID id) {
        categoryRepository.findById(id).ifPresent(cat -> {
            cat.setIsActive(false);
            categoryRepository.save(cat);
        });
    }
}