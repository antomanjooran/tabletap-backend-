package com.tabletap.service;

import com.tabletap.dto.response.*;
import com.tabletap.entity.Category;
import com.tabletap.entity.MenuItem;
import com.tabletap.exception.ApiException;
import com.tabletap.repository.CategoryRepository;
import com.tabletap.repository.MenuItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MenuService {

    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenu() {
        return categoryRepository.findActiveWithAvailableItems()
                .stream().map(this::toMenuResponse).toList();
    }

    @Transactional
    public void setAvailability(UUID menuItemId, boolean isAvailable) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ApiException("Menu item not found", "ITEM_NOT_FOUND", 404));
        item.setIsAvailable(isAvailable);
        menuItemRepository.save(item);
    }

    @Transactional
    public MenuItemResponse updateItem(UUID menuItemId, Map<String, Object> updates) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ApiException("Menu item not found", "ITEM_NOT_FOUND", 404));
        if (updates.containsKey("name"))        item.setName((String) updates.get("name"));
        if (updates.containsKey("description")) item.setDescription((String) updates.get("description"));
        if (updates.containsKey("emoji"))       item.setEmoji((String) updates.get("emoji"));
        return toItemResponse(menuItemRepository.save(item));
    }

    private MenuResponse toMenuResponse(Category cat) {
        return new MenuResponse(cat.getId(), cat.getName(), cat.getSortOrder(),
                cat.getMenuItems().stream().map(this::toItemResponse).toList());
    }

    private MenuItemResponse toItemResponse(MenuItem m) {
        return new MenuItemResponse(m.getId(), m.getName(), m.getDescription(),
                m.getPrice(), m.getEmoji(), m.getImageUrl(), m.getIsAvailable(), m.getSortOrder());
    }
}
