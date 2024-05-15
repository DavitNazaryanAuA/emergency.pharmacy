package com.capstone.emergency.pharmacy.core.vending.repository.mongo.model;

import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.Date;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
@Document(collection = "reservation")
@CompoundIndexes({
        @CompoundIndex(name = "user_item_index", def = "{'reserved_item' : 1, 'user_id' : 1}", unique = true)
})
public class Reservation {
    @Id
    @Builder.Default
    private String id = null;

    @Field("reserved_item")
    private ReservedItem reservedItem;

    @Field("user_id")
    private String userId;

    @Field("quantity")
    private Integer quantity;

    @Field("exp_date")
    private Date expDate;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    @Setter
    @ToString
    public static class ReservedItem{
        @Field("vending_machine_id")
        private String vendingMachineId;

        @Field("item_id")
        private String itemId;
    }
}
