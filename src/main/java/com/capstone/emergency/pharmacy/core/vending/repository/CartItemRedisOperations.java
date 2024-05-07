package com.capstone.emergency.pharmacy.core.vending.repository;

import com.capstone.emergency.pharmacy.core.error.ApiException;
import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.vending.repository.model.CartItem;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@RequiredArgsConstructor
@Repository
public class CartItemRedisOperations {

    @Value("${redis.cart.prefix}")
    private String cartItemPrefix;

    final private ListOperations<String, Object> listOperations;
    final private RedisTemplate<String, Object> redisTemplate;

    public void addItemToCart(String userId, CartItem item) {
        String key = cartItemPrefix + ":" + userId;
        final var previous = listOperations.range(key, 0, 1);
        if (previous != null && !previous.isEmpty()) {
            final var casted = (CartItem) previous.get(0);
            if (!casted.getVendingMachineId().equals(item.getVendingMachineId())) {
                throw new BadRequestException("Cant have items from different machines", ApiException.Reason.MULTIPLE_MACHINES_TO_CART);
            }
        }
        listOperations.leftPush(key, item);
        redisTemplate.expire(key, 15, TimeUnit.MINUTES);

        System.out.println("Item");
        System.out.println(listOperations.range(key, 0, 1));
        System.out.println("Exp");
        System.out.println(redisTemplate.getExpire(key));
    }
}
