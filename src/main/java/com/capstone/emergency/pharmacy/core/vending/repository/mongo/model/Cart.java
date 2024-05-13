package com.capstone.emergency.pharmacy.core.vending.repository.mongo.model;


import com.capstone.emergency.pharmacy.core.vending.repository.Orderable;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Document(collection = "cart")
public class Cart {

    @Id
    private String id = null;

    @Field("vending_machine_id")
    private Long vendingMachineId;

    @Field("user_id")
    @Indexed(name = "user_id_unique", unique = true)
    private String userId;

    @Field("cart_items")
    private List<CartItem> cartItems;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class CartItem implements Orderable {
        @Field("quantity")
        private Integer quantity;
        @Field("item_id")
        private Long itemId;
    }
}
