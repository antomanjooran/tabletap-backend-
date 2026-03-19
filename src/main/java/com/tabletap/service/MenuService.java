package com.tabletap.service;
import com.tabletap.dto.response.*;
import com.tabletap.entity.*;
import com.tabletap.exception.ApiException;
import com.tabletap.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Service @RequiredArgsConstructor
public class MenuService {
    private final CategoryRepository categoryRepository;
    private final MenuItemRepository menuItemRepository;

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenu() {
        return categoryRepository.findActiveWithAvailableItems().stream().map(this::toMenuResponse).toList();
    }

    @Transactional
    public MenuItemResponse updateQuantity(UUID menuItemId, Integer quantity) {
        MenuItem item = menuItemRepository.findById(menuItemId)
                .orElseThrow(() -> new ApiException("Menu item not found", "ITEM_NOT_FOUND", 404));
        item.setQuantityAvailable(quantity);
        if (quantity != null && quantity <= 0) item.setIsAvailable(false);
        else item.setIsAvailable(true);
        return toItemResponse(menuItemRepository.save(item));
    }

    @Transactional
    public void decrementStock(UUID menuItemId, int qty) {
        MenuItem item = menuItemRepository.findById(menuItemId).orElse(null);
        if (item == null || item.getQuantityAvailable() == null) return;
        int newQty = Math.max(0, item.getQuantityAvailable() - qty);
        item.setQuantityAvailable(newQty);
        if (newQty == 0) item.setIsAvailable(false);
        menuItemRepository.save(item);
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
        if (updates.containsKey("quantityAvailable")) {
            Object q = updates.get("quantityAvailable");
            Integer qty = q == null ? null : ((Number) q).intValue();
            item.setQuantityAvailable(qty);
            if (qty != null && qty <= 0) item.setIsAvailable(false);
            else item.setIsAvailable(true);
        }
        return toItemResponse(menuItemRepository.save(item));
    }

    public MenuResponse toMenuResponse(Category cat) {
        return new MenuResponse(cat.getId(), cat.getName(), cat.getSortOrder(),
                cat.getMenuItems().stream().map(this::toItemResponse).toList());
    }

    public MenuItemResponse toItemResponse(MenuItem m) {
        return new MenuItemResponse(m.getId(), m.getName(), m.getDescription(),
                m.getPrice(), m.getEmoji(), m.getImageUrl(), m.getIsAvailable(),
                m.getQuantityAvailable(), m.getSortOrder());
    }
}