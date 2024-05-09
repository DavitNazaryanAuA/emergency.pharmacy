package com.capstone.emergency.pharmacy.rest.controller.vending;

import com.capstone.emergency.pharmacy.core.vending.service.CartService;
import com.capstone.emergency.pharmacy.core.vending.service.model.Cart;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Validated
@RequiredArgsConstructor
@CrossOrigin
@RestController
@RequestMapping("/api/cart")
public class CartController {

    final private CartService cartService;

    @PostMapping("/vm/{vendingMachineId}/item/{itemId}")
    public ResponseEntity<Void> addItemToCard(
            @PathVariable("vendingMachineId") Long vendingMachineId,
            @PathVariable("itemId") Long itemId,

            @RequestParam("quantity")
            @NotNull
                    Integer quantity
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        cartService.addItemToCart(
                userId,
                vendingMachineId,
                itemId,
                quantity
        );
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/vm/{vendingMachineId}/item/{itemId}")
    public ResponseEntity<Void> removeCartItem(
            @PathVariable("vendingMachineId") Long vendingMachineId,
            @PathVariable("itemId") Long itemId,

            @RequestParam("quantity")
            @NotNull
                    Integer quantity
    ) {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        cartService.removeItemFromCart(
                userId,
                vendingMachineId,
                itemId,
                quantity
        );
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Cart> getCart() {
        final var auth = SecurityContextHolder.getContext().getAuthentication();
        final var jwt = (Jwt) auth.getPrincipal();
        final var userId = jwt.getSubject();

        return ResponseEntity.ok(
                cartService.getCartItems(userId)
        );
    }
}
