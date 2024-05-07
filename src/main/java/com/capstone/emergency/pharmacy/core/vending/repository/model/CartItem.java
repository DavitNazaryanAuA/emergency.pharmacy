package com.capstone.emergency.pharmacy.core.vending.repository.model;

import com.capstone.emergency.pharmacy.core.item.repository.model.Item;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Builder
public class CartItem implements Serializable {
    private Long vendingMachineId;
    private Integer quantity;
    private Item item;
}
