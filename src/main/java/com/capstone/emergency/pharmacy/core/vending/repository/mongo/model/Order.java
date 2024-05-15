package com.capstone.emergency.pharmacy.core.vending.repository.mongo.model;

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

    @Field("vending_machine_id")
    private String vendingMachineId;

    @Field("items")
    private List<OrderItem> items;

    @Field("total")
    private Double total;

    @Field("status")
    private Status status;

    @Field("date")
    private Date date = new Date();

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @ToString
    public static class OrderItem {

        @Field("item_id")
        private String itemId;

        @Field("quantity")
        private Integer quantity;

        @Field("price")
        private Double price;
    }

    public enum Status {
        PENDING, CANCELLED, COMPLETE;

        public boolean isPending() {
            return this == PENDING;
        }

        public boolean isCancelled() {
            return this == CANCELLED;
        }

        public boolean isComplete() {
            return this == COMPLETE;
        }
    }
}
