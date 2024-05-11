package com.capstone.emergency.pharmacy.core.vending.repository.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;
import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@ToString
@Document(collection = "order")
public class Order {

    @Id
    private String id = null;

    @Field("user_id")
    private String userId;

    @Field("items")
    private List<OrderItem> items;

    @Field("total")
    private Double total;

    @Field("paid")
    private Boolean paid;

    @Field("date")
    private Date date = new Date();

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public static class OrderItem {
        @Field("vending_machine_id")
        private String vendingMachineId;

        @Field("item_id")
        private String itemId;

        @Field("quantity")
        private Integer quantity;

        @Field("price")
        Double price;
    }
}
