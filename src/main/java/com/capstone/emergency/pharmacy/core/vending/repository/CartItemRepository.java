package com.capstone.emergency.pharmacy.core.vending.repository;

import com.capstone.emergency.pharmacy.core.vending.repository.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Repository
public class CartItemRepository {

    @Value("${redis.cart.prefix}")
    private String cartItemPrefix;

    final private RedisTemplate<String, Object> redisTemplate;
    final private HashOperations<String, String, Integer> hashOperations;

    public Integer itemQuantityInCart(String userId, Long itemId, Long vendingMachineId) {
        String hashKey = cartItemPrefix + ":" + userId;
        String key = itemId.toString() + ":" + vendingMachineId;
        Integer inCart = hashOperations.get(hashKey, key);

        return inCart == null ? 0 : inCart;
    }

    public void addItemToCart(String userId, Long itemId, Long vendingMachineId, Integer quantity) {
        String hashKey = cartItemPrefix + ":" + userId;
        String key = itemId.toString() + ":" + vendingMachineId;

        hashOperations.put(hashKey, key, quantity);
        redisTemplate.expire(hashKey, 15, TimeUnit.MINUTES);
    }

    public void removeItemFromCart(String userId, Long itemId, Long vendingMachineId, Integer quantity) {
        String hashKey = cartItemPrefix + ":" + userId;
        String key = itemId.toString() + ":" + vendingMachineId;

        final var existing = hashOperations.get(hashKey, key);
        if (existing == null) return;
        if (existing > quantity) {
            hashOperations.put(hashKey, key, existing - quantity);
        } else {
            hashOperations.delete(hashKey, key);
        }
        redisTemplate.expire(hashKey, 15, TimeUnit.MINUTES);
    }

    public List<CartItem> getCartItems(String userId) {
        String key = cartItemPrefix + ":" + userId;
        final var items = hashOperations.entries(key);
        return items.entrySet().stream().map(entry -> {
                    final var ids = entry.getKey().split(":");
                    return CartItem
                            .builder()
                            .itemId(Long.parseLong(ids[0]))
                            .vendingMachineId(Long.parseLong(ids[1]))
                            .quantity(entry.getValue())
                            .build();
                }
        ).toList();
    }
}
