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
@ToString
@Document(collection = "reservation")
@CompoundIndexes({
        @CompoundIndex(name = "user_item_index", def = "{'user_id' : 1, 'item_id' : 1}", unique = true)
})
public class Reservation {
    @Id
    private String id = null;

    @Field("item_id")
    private String itemId;

    @Field("user_id")
    private String userId;

    @Field("quantity")
    private Integer quantity;

    @Field("exp_date")
    private Date date;
}
