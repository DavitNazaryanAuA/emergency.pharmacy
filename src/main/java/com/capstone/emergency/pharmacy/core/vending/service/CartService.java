package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.ApiException;
import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.item.repository.ItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.CartRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.mongo.model.Cart;
import com.capstone.emergency.pharmacy.core.vending.service.model.CartResponse;
import com.capstone.emergency.pharmacy.rest.controller.item.model.mapper.ItemDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CartService {

    private final VendingMachineItemRepository vendingMachineItemRepository;
    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final ItemDtoMapper itemDtoMapper;

    public void addItemToCart(
            String userId,
            Long vendingMachineId,
            Long itemId,
            Integer quantity
    ) {
        final var machineItem = vendingMachineItemRepository
                .findByVendingMachineIdAndItem_Id(vendingMachineId, itemId)
                .orElseThrow(() -> new NotFoundException(
                        "Item" + itemId + " not found in machine: " + vendingMachineId
                ));

        final var inMachine = machineItem.getQuantity();

        var cart = cartRepository.findByUserIdAndAndVendingMachineId(userId, vendingMachineId);

        var inCart = 0;
        Cart.CartItem cartItem = null;
        if (cart != null) {
            cartItem = cart.getCartItems()
                    .stream()
                    .filter(item -> Objects.equals(item.getItemId(), itemId))
                    .findFirst()
                    .orElse(null);

            inCart = cartItem != null ? cartItem.getQuantity() : 0;
        } else {
            cart = Cart.builder()
                    .userId(userId)
                    .vendingMachineId(vendingMachineId)
                    .cartItems(new ArrayList<>())
                    .build();
        }

        if (inMachine - inCart < quantity) {
            throw new BadRequestException(
                    "Not enough items: " + machineItem.getQuantity() +
                            ", requested: " + quantity +
                            ", in cart: " + inCart,
                    ApiException.Reason.NOT_ENOUGH_ITEMS
            );
        }

        if (cartItem != null) {
            cartItem.setQuantity(quantity + inCart);
        } else {
            cart.getCartItems().add(
                    Cart.CartItem.builder()
                            .itemId(itemId)
                            .quantity(quantity + inCart)
                            .build()
            );
        }

        cartRepository.save(cart);
    }

    public void removeItemFromCart(
            String userId,
            Long vendingMachineId,
            Long itemId,
            Integer quantity
    ) {
        final var cart = cartRepository.findByUserIdAndAndVendingMachineId(userId, vendingMachineId);
        if (cart == null) {
            throw new NotFoundException("Cart not found");
        }

        final var cartItem = cart.getCartItems()
                .stream()
                .filter(item -> Objects.equals(item.getItemId(), itemId))
                .findFirst()
                .orElse(null);

        if (cartItem == null) {
            throw new NotFoundException("Item not found in cart");
        }

        if (cartItem.getQuantity() - quantity <= 0) {
            cart.getCartItems().remove(cartItem);

            if (cart.getCartItems().isEmpty()) {
                cartRepository.delete(cart);
                return;
            }
        } else {
            cartItem.setQuantity(cartItem.getQuantity() - quantity);
        }

        cartRepository.save(cart);
    }

    public CartResponse getCartItems(String userId) {
        final var cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Cart not found"));
        final var cartItems = cart.getCartItems();
        final var itemIds = cartItems.stream().map(Cart.CartItem::getItemId).toList();
        final var items = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> itemRepository.findById(id).get()))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .toList();

        final var cartItemResponseList = IntStream.range(0, items.size()).mapToObj(i ->
                new CartResponse.CartItemResponse(
                        cart.getVendingMachineId(),
                        itemDtoMapper.toItemResponse(items.get(i)),
                        cartItems.get(i).getQuantity()
                )
        ).toList();
        return new CartResponse(cartItemResponseList);
    }
}
