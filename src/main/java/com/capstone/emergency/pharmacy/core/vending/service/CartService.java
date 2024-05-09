package com.capstone.emergency.pharmacy.core.vending.service;

import com.capstone.emergency.pharmacy.core.error.ApiException;
import com.capstone.emergency.pharmacy.core.error.BadRequestException;
import com.capstone.emergency.pharmacy.core.error.NotFoundException;
import com.capstone.emergency.pharmacy.core.item.repository.ItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.CartItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.VendingMachineItemRepository;
import com.capstone.emergency.pharmacy.core.vending.repository.model.CartItem;
import com.capstone.emergency.pharmacy.core.vending.service.model.Cart;
import com.capstone.emergency.pharmacy.rest.controller.item.model.mapper.ItemDtoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class CartService {

    private final VendingMachineItemRepository vendingMachineItemRepository;
    private final ItemRepository itemRepository;
    private final CartItemRepository cartItemRepository;
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
        final var inCart = cartItemRepository.itemQuantityInCart(userId, itemId, vendingMachineId);

        if (inMachine - inCart < quantity) {
            throw new BadRequestException(
                    "Not enough items: " + machineItem.getQuantity() +
                            ", requested: " + quantity +
                            ", in cart: " + inCart,
                    ApiException.Reason.NOT_ENOUGH_ITEMS
            );
        }

        cartItemRepository.addItemToCart(userId, itemId, vendingMachineId, quantity + inCart);
    }

    public void removeItemFromCart(
            String userId,
            Long vendingMachineId,
            Long itemId,
            Integer quantity
    ) {
        cartItemRepository.removeItemFromCart(userId, itemId, vendingMachineId, quantity);
    }

    public Cart getCartItems(String userId) {
        final var cartItems = cartItemRepository.getCartItems(userId);
        final var itemIds = cartItems.stream().map(CartItem::getItemId).toList();
        final var items = itemIds.stream()
                .map(id -> CompletableFuture.supplyAsync(() -> itemRepository.findById(id).get()))
                .toList()
                .stream()
                .map(CompletableFuture::join)
                .toList();

        final var cartItemResponseList = IntStream.range(0, items.size()).mapToObj(i ->
                new Cart.CartItemResponse(
                        cartItems.get(i).getVendingMachineId(),
                        itemDtoMapper.toItemResponse(items.get(i)),
                        cartItems.get(i).getQuantity()
                )
        ).toList();
        return new Cart(cartItemResponseList);
    }
}
