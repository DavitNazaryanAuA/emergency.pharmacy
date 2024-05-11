package com.capstone.emergency.pharmacy.core.vending.repository.model;

import com.capstone.emergency.pharmacy.core.vending.repository.Orderable;
import lombok.*;

import java.io.Serializable;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartItem implements Serializable, Orderable {
    private Long vendingMachineId;
    private Integer quantity;
    private Long itemId;
}
